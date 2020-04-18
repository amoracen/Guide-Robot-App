package fau.amoracen.guiderobot.ui.dashboardactivity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.Objects;

import fau.amoracen.guiderobot.R;
import fau.amoracen.guiderobot.service.BluetoothConnection;
import fau.amoracen.guiderobot.service.BluetoothState;

/**
 * PairDevices Fragment  is responsible for pairing the phone and the server
 */
public class PairDevicesFragment extends Fragment {

    private ListView devicesList;
    private Button scanningButton;
    private TextView feedbackTextView;
    private ArrayAdapter<String> listAdapter;
    private static final int REQUEST_ENABLE_PairDevices = 11;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 1;
    private Boolean deviceFound = false;
    private Boolean calledScan = false;
    private BluetoothConnection myConnection;
    private BluetoothState bluetoothState;
    /**
     * Add on Item click listener to pair to the selected device
     */
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
            final BluetoothDevice device = bluetoothState.getBluetoothAdapter().getRemoteDevice(address);
            try {
                /*Start a new Connection*/
                myConnection = new BluetoothConnection(device);
                myConnection.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    /**
     * Implementing receiver to get devices detected
     */
    private final BroadcastReceiver devicesFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action) && calledScan) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if ("guide-robot".equals(device.getName())) {
                    //listAdapter.add(device.getName());
                    listAdapter.add(device.getName() + "\n" + device.getAddress());
                    listAdapter.notifyDataSetChanged();
                    devicesList.setOnItemClickListener(myListClickListener);
                    deviceFound = true;
                    bluetoothState.cancelDiscovery();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) && calledScan) {
                scanningButton.setText(R.string.start_scanning);
                if (!deviceFound) {
                    listAdapter.add("Device Not Found");
                }
                feedbackTextView.setText(R.string.scanning_completed);
                calledScan = false;
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action) && calledScan) {
                scanningButton.setText(R.string.scanning_in_progress);
                feedbackTextView.setVisibility(View.VISIBLE);
                feedbackTextView.setText(R.string.scanning_in_progress);
                feedbackTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pair_devices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        devicesList = view.findViewById(R.id.devicesList);
        scanningButton = view.findViewById(R.id.scanningBtn);
        feedbackTextView = view.findViewById(R.id.feedback_text_view);

        //Array adapter to display list of device
        listAdapter = new ArrayAdapter<String>(Objects.requireNonNull(getContext()), android.R.layout.simple_expandable_list_item_1);
        devicesList.setAdapter(listAdapter);

        //Get bluetooth adapter
        bluetoothState = BluetoothState.getInstance(BluetoothAdapter.getDefaultAdapter());
        checkBluetoothState();
        //register a dedicated receiver for some Bluetooth actions
        Objects.requireNonNull(getActivity()).registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        getActivity().registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        getActivity().registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));


        scanningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothState.checkBluetoothState()) {
                    startScanningForDevices();
                } else {
                    scanningButton.setEnabled(false);
                    listAdapter.clear();
                    feedbackTextView.setVisibility(View.INVISIBLE);
                    checkBluetoothState();
                }
            }
        });
    }//EOF onViewCreated

    /**
     * Coarse Location permission needed to paired devices
     */
    private void checkCoarseLocationPermission() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
        }
    }

    /**
     * Start Scanning
     */
    private void startScanningForDevices() {
        if (bluetoothState.BluetoothSupported() && bluetoothState.checkBluetoothState()) {
            listAdapter.clear();
            calledScan = true;
            bluetoothState.startDiscovery();
        } else {
            calledScan = false;
            checkBluetoothState();
        }
    }

    /**
     * Checking if Bluetooth is Enabled
     */
    private void checkBluetoothState() {
        if (!bluetoothState.BluetoothSupported()) {
            createToast("Bluetooth is not supported on your device!");
            scanningButton.setEnabled(false);
        } else {
            if (bluetoothState.checkBluetoothState()) {
                scanningButton.setEnabled(true);
            } else {
                startBluetoothIntent();
            }
            //check permission at start of the app
            checkCoarseLocationPermission();
        }
    }

    /**
     * Start Intent to request permission to use Bluetooth
     */
    private void startBluetoothIntent() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_PairDevices);
    }

    /**
     * Checking user response
     *
     * @param requestCode integer
     * @param resultCode  integer
     * @param data        Intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_PairDevices) {
            if (resultCode == Activity.RESULT_OK) {
                scanningButton.setEnabled(true);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                createToast("Bluetooth is not Enable");
                scanningButton.setEnabled(false);
            }
        }
    }

    /**
     * Create a Toast message
     *
     * @param message a string
     */
    private void createToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (myConnection != null) {
            myConnection.closeSocket();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            Objects.requireNonNull(getActivity()).unregisterReceiver(devicesFoundReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}//EOF Class
