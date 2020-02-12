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
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class DashboardFragment extends Fragment {

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
    public static final int REQUEST_ENABLE_BLUETOOTH = 11;
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
        feedbackTextView = view.findViewById(R.id.feedback_text_view);

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
                        Toast.makeText(getContext(), "Send a Command Failed", Toast.LENGTH_LONG).show();
                        connectThread = null;
                        feedbackTextView.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        //Get bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetoothState();
    }//EOF onViewCreated

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

    private void checkBluetoothState() {
        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not supported on your device!", Toast.LENGTH_SHORT).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(getActivity(), "You Need to Enable Bluetooth", Toast.LENGTH_SHORT).show();
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
            } else {
                checkPairedDevices();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(getContext(), "Turned on", Toast.LENGTH_LONG).show();
            checkPairedDevices();
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getContext(), "Turned off", Toast.LENGTH_LONG).show();
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
                Intent toPairDevice = new Intent(getActivity(), PairDeviceActivity.class);
                startActivity(toPairDevice);
            } else {
                bluetoothTextView.setText(deviceName);
                bluetoothImageView.setImageResource(R.drawable.ic_bluetooth_connected);
                bluetoothImageView.setContentDescription("Paired to " + deviceName);
            }
        } else {
            //Toast.makeText(this, "There are not paired devices", Toast.LENGTH_SHORT).show();
            Intent toPairDevice = new Intent(getActivity(), PairDeviceActivity.class);
            startActivity(toPairDevice);
        }
    }
}
