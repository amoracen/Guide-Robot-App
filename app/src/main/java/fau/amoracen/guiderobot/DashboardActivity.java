package fau.amoracen.guiderobot;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class DashboardActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private String deviceName;
    private String deviceAddress;
    private TextView bluetoothTextView;
    private ImageView bluetoothImageView;
    private EditText commandEditText;
    private TextView feedbackTextView;
    private Button sendCommandButton;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private ConnectThread connectThread;
    private boolean doubleBackToExitPressedOnce = false;
    public static final int REQUEST_ENABLE_BLUETOOTH = 11;
    private boolean bluetoothStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bluetoothImageView = findViewById(R.id.bluetoothLogo);
        bluetoothTextView = findViewById(R.id.bluetoothTextView);
        commandEditText = findViewById(R.id.commandEditText);
        sendCommandButton = findViewById(R.id.sendButton);
        feedbackTextView = findViewById(R.id.feedback_text_view);

        //Get bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetoothState();
    }//EOF onCreate

    private class ConnectThread extends Thread {

        private ConnectThread(BluetoothDevice device) throws IOException {
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

        public void send(String msg) throws IOException {
            OutputStream mmOutputStream = mmSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());
            //receive();
        }

        /*public void receive() throws IOException {
            InputStream mmInputStream = mmSocket.getInputStream();
            byte[] buffer = new byte[256];
            int bytes;

            try {
                bytes = mmInputStream.read(buffer);
                String readMessage = new String(buffer, 0, bytes);
                Log.d(TAG, "Received: " + readMessage);
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Problems occurred!");
                return;
            }
        }*/
    }

    public void sendCommand(View view) {
        String msg = commandEditText.getText().toString();
        if (msg.isEmpty()) {
            Toast.makeText(this, "Please enter a Command", Toast.LENGTH_LONG).show();
            commandEditText.requestFocus();
        } else {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            try {
                if (connectThread == null) {
                    connectThread = new ConnectThread(device);
                    connectThread.start();
                }
                connectThread.send(msg);
                feedbackTextView.setVisibility(View.VISIBLE);
                feedbackTextView.setText("Command Sent");
                feedbackTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Send a Command Failed", Toast.LENGTH_LONG).show();
                connectThread = null;
                feedbackTextView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void checkBluetoothState() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on your device!", Toast.LENGTH_SHORT).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "You Need to Enable Bluetooth", Toast.LENGTH_SHORT).show();
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
            } else {
                checkPairedDevices();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
            checkPairedDevices();
        }
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_LONG).show();
            sendCommandButton.setEnabled(false);
        }
    }

    public void checkPairedDevices() {
       /* //Get bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Check Bluetooth State
        checkBluetoothState();*/
        //Check paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            boolean deviceFound = false;
            //Toast.makeText(this, "There are paired devices!", Toast.LENGTH_LONG).show();
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                Log.i("Devices Paired", device.getName());
                if ("jetson-nano".equals(device.getName())) {
                    deviceName = device.getName();
                    deviceAddress = device.getAddress();
                    deviceFound = true;
                    //String deviceName = device.getName();
                    //String deviceHardwareAddress = device.getAddress(); // MAC address
                }
            }
            if (!deviceFound) {
                Intent toPairDevice = new Intent(this, PairDeviceActivity.class);
                startActivity(toPairDevice);
            } else {
                bluetoothTextView.setText(deviceName);
                bluetoothImageView.setImageResource(R.drawable.ic_bluetooth_connected);
                bluetoothImageView.setContentDescription("Paired to " + deviceName);
            }
        } else {
            //Toast.makeText(this, "There are not paired devices", Toast.LENGTH_SHORT).show();
            Intent toPairDevice = new Intent(this, PairDeviceActivity.class);
            startActivity(toPairDevice);
        }
    }


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            //Do Nothing to disable back button
            //this.finish();
            finishAffinity();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

}//EOF CLASS
