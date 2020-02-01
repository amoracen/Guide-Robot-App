package fau.amoracen.guiderobot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class PairDeviceActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ListView devicesList;
    private Button scanningButton;
    private TextView feedbackTextView;
    private ArrayAdapter<String> listAdapter;
    public static final int REQUEST_ENABLE_BLUETOOTH = 11;
    public static final int REQUEST_ACCESS_COARSE_LOCATION =1;
    private Boolean deviceFound = false;
    private BluetoothDevice mmDevice;
    private BluetoothSocket mmSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_device);

        //Get bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        devicesList = findViewById(R.id.devicesList);
        scanningButton = findViewById(R.id.scanningBtn);
        feedbackTextView = findViewById(R.id.feedback_text_view);

        //Array adapter to display list of device
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1);
        devicesList.setAdapter(listAdapter);

        //Check Bluetooth State
        checkBluetoothState();
        //register a dedicated receiver for some Bluetooth actions
        registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        //check permission at start of the app
        checkCoarseLocationPermission();
    }//EOF onCreate

    private boolean checkCoarseLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
            return false;
        }else{
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case REQUEST_ACCESS_COARSE_LOCATION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Access coarse location allowed. You can scan bluetooth devices", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Access coarse location not allowed. You can not scan bluetooth devices", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void startScanningForDevices(View view) {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            listAdapter.clear();
            bluetoothAdapter.startDiscovery();
        } else {
            checkBluetoothState();
        }
    }

    public void checkBluetoothState() {
        //Check Bluetooth State
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on your device!", Toast.LENGTH_SHORT).show();
        } else {
            if (bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "Bluetooth is enable", Toast.LENGTH_SHORT).show();
                scanningButton.setEnabled(true);
            } else {
                Toast.makeText(this, "You Need to Enable Bluetooth", Toast.LENGTH_SHORT).show();
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            unregisterReceiver(devicesFoundReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //implement our receiver to get devices detected
    private final BroadcastReceiver devicesFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if("jetson-nano".equals(device.getName())) {
                    //listAdapter.add(device.getName());
                    listAdapter.add(device.getName() + "\n" + device.getAddress());
                    listAdapter.notifyDataSetChanged();
                    devicesList.setOnItemClickListener(myListClickListener);
                    deviceFound = true;
                }
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                scanningButton.setText(R.string.start_scanning);
                if(!deviceFound){
                    listAdapter.add("Device Not Found");
                }
                feedbackTextView.setText(R.string.scanning_completed);
            }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                scanningButton.setText(R.string.scanning_in_progress);
                feedbackTextView.setVisibility(View.VISIBLE);
                feedbackTextView.setText(R.string.scanning_in_progress);
                feedbackTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        }
    };
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            try {
                new ConnectThread(device).start();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    };
    private class ConnectThread extends Thread{
        private ConnectThread(BluetoothDevice device) throws IOException{
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e("Fail*************", "Socket's create() method failed", e);
            }
            mmSocket = tmp;
            bluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                Log.v("Fail*************", "Connection exception!");
                mmSocket.close();
            }
        }
    }//EOF Class ConnectThread
    @Override
    protected void onStop() {
        super.onStop();
        try {
            if(mmSocket != null) {
                mmSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}//EOF Class
