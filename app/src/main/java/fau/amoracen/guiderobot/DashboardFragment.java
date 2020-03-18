package fau.amoracen.guiderobot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
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
    private BluetoothConnection myConnection;
    private boolean destroyed;
    private boolean connected = false;
    private String messageToSend;
    private static final String SUCCESS = "CONNECTED";
    private static final String SUCCESS_MSG = "MESSAGE_SENT";
    private static final String FAILED = "NOT CONNECTED";
    private static final String FAILED_MSG = "MESSAGE_FAILED";


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
        Button startConnection = view.findViewById(R.id.startConnectionButton);
        Button goForwardBTN = view.findViewById(R.id.goForwardButton);
        Button goInCirclesBTN = view.findViewById(R.id.goInCirclesButton);
        Button drawSquareBTN = view.findViewById(R.id.drawSquareButton);
        destroyed = false;
        //Get bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetoothState();

        startConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection startConnection = new startConnection();
                startConnection.execute();
            }
        });

        sendCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageToSend = commandEditText.getText().toString();
                if (messageToSend.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a Command", Toast.LENGTH_LONG).show();
                    commandEditText.requestFocus();
                } else {
                    sendMessage sendMessage = new sendMessage();
                    sendMessage.execute();
                }
            }
        });
        goForwardBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageToSend = "GO_FORWARD";
                sendMessage sendMessage = new sendMessage();
                sendMessage.execute();
            }
        });
        goInCirclesBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageToSend = "GO_IN_CIRCLES";
                sendMessage sendMessage = new sendMessage();
                sendMessage.execute();
            }
        });
        drawSquareBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageToSend = "DRAW_SQUARE";
                sendMessage sendMessage = new sendMessage();
                sendMessage.execute();
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
            bluetoothTextView.setContentDescription("Pair to " + deviceName);
            //Check Connection on the background
            startConnection startConnection = new startConnection();
            startConnection.execute();
        } else {
            Toast.makeText(getContext(), "There are not paired devices", Toast.LENGTH_SHORT).show();
            sendCommandButton.setEnabled(false);
        }
    }


    /**
     * Start Connection to server
     */
    public class startConnection extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                /*Start a new Connection*/
                final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                myConnection = new BluetoothConnection(device);
                myConnection.start();
                connected = true;
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("Exception", "Failed to Connect");
                connected = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (connected) {
                updateUIMessage(SUCCESS);
                myConnection.closeSocket();
            } else {
                updateUIMessage(FAILED);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destroyed = true;
        if (myConnection != null) {
            myConnection.closeSocket();
        }
    }

    /**
     * Start Connection to server
     */
    public class sendMessage extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                /*Start a new Connection*/
                final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                myConnection = new BluetoothConnection(device);
                myConnection.start();
                myConnection.send(messageToSend);
                connected = true;
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("Exception", "Failed to Connect");
                connected = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (connected) {
                updateUIMessage(SUCCESS_MSG);
                myConnection.closeSocket();
            } else {
                updateUIMessage(FAILED_MSG);
            }

        }
    }

    /**
     * Update UI after AsyncTask is executed
     *
     * @param connection a string representing the connection
     */
    public void updateUIMessage(String connection) {
        //Check if user change fragments
        if (destroyed) {
            return;
        }
        feedbackTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        switch (connection) {
            case SUCCESS:
                //Update bluetooth logo
                bluetoothImageView.setImageResource(R.drawable.ic_bluetooth_connected);
                bluetoothImageView.setContentDescription("Connected to " + deviceName);
                //Update message
                feedbackTextView.setVisibility(View.VISIBLE);
                feedbackTextView.setText(R.string.connection_success);
                break;
            case FAILED:
                myConnection = null;
                //Update message
                feedbackTextView.setVisibility(View.VISIBLE);
                feedbackTextView.setText(R.string.connection_failed);
                break;
            case SUCCESS_MSG:
                feedbackTextView.setVisibility(View.VISIBLE);
                feedbackTextView.setText(R.string.command_sent);
                break;
            case FAILED_MSG:
                myConnection = null;
                feedbackTextView.setVisibility(View.VISIBLE);
                feedbackTextView.setText(R.string.command_failed);
                break;
        }
    }
}//EOF Class
