package fau.amoracen.guiderobot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.Set;

/**
 * Dashboard Fragment
 */
public class DashboardFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private String deviceName;
    private String deviceAddress;
    private TextView bluetoothTextView;
    private ImageView bluetoothImageView;

    private EditText commandEditText;
    private TextView feedbackTextView;

    private static final int REQUEST_ENABLE_BLUETOOTH = 11;
    private Button sendCommandButton;
    private Button startConnection;
    private BluetoothConnection myConnection;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    //private ConnectThread connectThread;
    private boolean bluetoothStatus = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        bluetoothImageView = view.findViewById(R.id.bluetoothLogo);
        bluetoothTextView = view.findViewById(R.id.bluetoothTextView);
        commandEditText = view.findViewById(R.id.commandEditText);
        sendCommandButton = view.findViewById(R.id.sendButton);
        startConnection = view.findViewById(R.id.startConnectionButton);
        feedbackTextView = view.findViewById(R.id.feedback_text_view);
        //Get bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetoothState();

        startConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();
            }
        });

        sendCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = commandEditText.getText().toString();
                if (msg.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a Command", Toast.LENGTH_LONG).show();
                    commandEditText.requestFocus();
                } else {
                    final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                    try {
                        if (myConnection == null) {
                            myConnection = new BluetoothConnection(device);
                            myConnection.start();
                        }
                        myConnection.send(msg);
                        feedbackTextView.setVisibility(View.VISIBLE);
                        feedbackTextView.setText(R.string.command_sent);
                        feedbackTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Send a Command Failed", Toast.LENGTH_LONG).show();
                        myConnection = null;
                        feedbackTextView.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }//EOF onViewCreated

    /**
     * Checking if Bluetooth is Enabled
     */
    private void checkBluetoothState() {
        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth is not supported on your device!", Toast.LENGTH_SHORT).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                //Toast.makeText(getActivity(), "You Need to Enable Bluetooth", Toast.LENGTH_SHORT).show();
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
            } else {
                bluetoothImageView.setImageResource(R.drawable.ic_bluetooth_enabled);
                bluetoothImageView.setContentDescription("Bluetooth is Enable");
                checkPairedDevices();
            }
        }
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
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(getContext(), "Bluetooth is On", Toast.LENGTH_LONG).show();
            bluetoothImageView.setImageResource(R.drawable.ic_bluetooth_enabled);
            bluetoothImageView.setContentDescription("Bluetooth is Enable");
            checkPairedDevices();
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getContext(), "Bluetooth is Off", Toast.LENGTH_LONG).show();
            sendCommandButton.setEnabled(false);
        }
    }

    /**
     * Checking if the devices were paired
     */
    public void checkPairedDevices() {
        //Check paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        boolean deviceFound = false;
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                Log.i("Devices Paired", device.getName());
                if ("guide-robot".equals(device.getName())) {
                    deviceName = device.getName();
                    deviceAddress = device.getAddress();
                    deviceFound = true;
                }
            }
        }

        if (deviceFound) {
            bluetoothTextView.setText(deviceName.toUpperCase());
        } else {
            Toast.makeText(getContext(), "There are not paired devices", Toast.LENGTH_SHORT).show();
            sendCommandButton.setEnabled(false);
        }
    }

    /**
     * Start a new Connection with the  server.
     */
    public void startConnection() {
        try {
            /*Start a new Connection*/
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            myConnection = new BluetoothConnection(device);
            myConnection.start();
            bluetoothImageView.setImageResource(R.drawable.ic_bluetooth_connected);
            bluetoothImageView.setContentDescription("Connected to " + deviceName);
            feedbackTextView.setVisibility(View.VISIBLE);
            feedbackTextView.setText(R.string.connection_success);
            feedbackTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Exception", "Failed to Connect");
            feedbackTextView.setVisibility(View.VISIBLE);
            feedbackTextView.setText(R.string.connection_failed);
            feedbackTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
    }


    /**
     * ConnectThread is responsible for sending and receiving information to the Jetson Nano
     */
    /*private class ConnectThread extends Thread {

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




    }*/

}//EOF Class
