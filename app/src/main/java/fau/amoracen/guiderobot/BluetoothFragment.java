package fau.amoracen.guiderobot;

import android.Manifest;
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

/**
 * Bluetooth Fragment is responsible for pairing the phone and the jetson nano
 */
public class BluetoothFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private ListView devicesList;
    private Button scanningButton;
    private TextView feedbackTextView;
    private ArrayAdapter<String> listAdapter;
    private static final int REQUEST_ENABLE_BLUETOOTH = 11;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 1;
    private Boolean deviceFound = false;
    private BluetoothConnection myConnection;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Get bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        devicesList = view.findViewById(R.id.devicesList);
        scanningButton = view.findViewById(R.id.scanningBtn);
        feedbackTextView = view.findViewById(R.id.feedback_text_view);

        //Array adapter to display list of device
        listAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_expandable_list_item_1);
        devicesList.setAdapter(listAdapter);

        //Check Bluetooth State
        checkBluetoothState();
        //register a dedicated receiver for some Bluetooth actions
        getActivity().registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        getActivity().registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        getActivity().registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        //check permission at start of the app
        checkCoarseLocationPermission();

        scanningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScanningForDevices();
            }
        });
    }//EOF onViewCreated

    /**
     * Coarse Location permission needed to paired devices
     */
    private boolean checkCoarseLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
            //scanningButton.setEnabled(false);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Start Scanning
     */
    public void startScanningForDevices() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            listAdapter.clear();
            bluetoothAdapter.startDiscovery();
        } else {
            checkBluetoothState();
        }
    }

    /**
     * Check if Bluetooth is enable
     */
    public void checkBluetoothState() {
        //Check Bluetooth State
        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth is not supported on your device!", Toast.LENGTH_SHORT).show();
        } else {
            if (bluetoothAdapter.isEnabled()) {
                scanningButton.setEnabled(true);
            } else {
                //Toast.makeText(getContext(), "You Need to Enable Bluetooth", Toast.LENGTH_SHORT).show();
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }

    /**
     * Implementing receiver to get devices detected
     */
    private final BroadcastReceiver devicesFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if ("guide-robot".equals(device.getName())) {
                    //listAdapter.add(device.getName());
                    listAdapter.add(device.getName() + "\n" + device.getAddress());
                    listAdapter.notifyDataSetChanged();
                    devicesList.setOnItemClickListener(myListClickListener);
                    deviceFound = true;
                    bluetoothAdapter.cancelDiscovery();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                scanningButton.setText(R.string.start_scanning);
                if (!deviceFound) {
                    listAdapter.add("Device Not Found");
                }
                feedbackTextView.setText(R.string.scanning_completed);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                scanningButton.setText(R.string.scanning_in_progress);
                feedbackTextView.setVisibility(View.VISIBLE);
                feedbackTextView.setText(R.string.scanning_in_progress);
                feedbackTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        }
    };

    /**
     * Add on Item click listener to pair to the selected device
     */
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            try {
                /*Start a new Connection*/
                myConnection = new BluetoothConnection(device);
                myConnection.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

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
            getActivity().unregisterReceiver(devicesFoundReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}//EOF Class
