package fau.amoracen.guiderobot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;

/**
 * Dashboard Fragment
 */
public class DashboardFragment extends Fragment {

    private static final int REQUEST_ENABLE_BLUETOOTH_Fragment = 2;
    /*UI Widgets*/
    private TextView bluetoothTextView;
    private ImageView bluetoothImageView;
    private TextView feedbackTextView;
    /*Bluetooth*/
    private BluetoothConnection myConnection;
    private BluetoothDevice myDevice;
    private BluetoothState bluetoothState;
    private boolean destroyed;
    private boolean connected = false;
    private String messageToSend;
    private static final String SUCCESS = "CONNECTED";
    private static final String SUCCESS_MSG = "MESSAGE_SENT";
    private static final String FAILED = "NOT CONNECTED";
    private static final String FAILED_MSG = "MESSAGE_FAILED";
    private static final String BluetoothEnable = "BluetoothEnable";
    private FloatingActionButton micButton;
    private SpeechToTextService speechToTextService;


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
        feedbackTextView = view.findViewById(R.id.feedback_text_view);
        destroyed = false;
        //Get bluetooth adapter
        bluetoothState = BluetoothState.getInstance(BluetoothAdapter.getDefaultAdapter());
        checkBluetoothState();

        Button doorButton = view.findViewById(R.id.doorButton);
        doorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setConnection("OPTION1");
            }
        });
        Button option2Button = view.findViewById(R.id.option2Button);
        option2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setConnection("OPTION2");
            }
        });
        Button option3Button = view.findViewById(R.id.option3Button);
        option3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setConnection("OPTION3");
            }
        });
        Button shutdownBTN = view.findViewById(R.id.shutdownButton);
        shutdownBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setConnection("STOP");
            }
        });
        micButton = view.findViewById(R.id.micButton);
        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechToTextService.startListening(getActivity());
            }
        });
        initializeSpeechRecognizer();
    }//EOF onViewCreated

    /**
     * Initialize the Speech Recognition Listener
     */
    private void initializeSpeechRecognizer() {
        speechToTextService = new SpeechToTextService(getContext());
        if (!speechToTextService.isRecognitionAvailable()) {
            micButton.setImageResource(R.drawable.ic_mic_off_blue_75dp);
            micButton.setContentDescription(getString(R.string.mic_description_dashboard));
            micButton.setEnabled(false);
            return;
        }
        speechToTextService.getSpeechRecognizer().setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                List<String> result_arr = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                assert result_arr != null;
                processInput(result_arr.get(0).toLowerCase());
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
    }

    /**
     * Create a connection to the server using the user's input
     *
     * @param input a string
     */
    private void processInput(String input) {
        String search = "take";
        if (input.contains(search)) {
            if (input.contains("door")) {
                createToast("Take me to: -> " + input);
                setConnection("OPTION1");
            } else if (input.contains("table")) {
                createToast("Take me to: -> " + input);
                setConnection("OPTION2");
            } else if (input.contains("restroom")) {
                createToast("Take me to: -> " + input);
                setConnection("OPTION3");
            } else {
                createToast(input);
            }
        } else {
            if (input.contains("shut down")) {
                createToast("Take me to: -> " + input);
                setConnection("STOP");
            } else {
                createToast(input);
            }
        }
    }

    /**
     * Start Intent to request permission to use Bluetooth
     */
    private void startBluetoothIntent() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH_Fragment);
    }

    /**
     * Create a Toast message
     *
     * @param message a string
     */
    private void createToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Checking if Bluetooth is Enabled
     */
    private void checkBluetoothState() {
        if (!bluetoothState.BluetoothSupported()) {
            createToast("Bluetooth is not supported on your device!");
        } else {
            if (bluetoothState.checkBluetoothState()) {
                updateUIButtons(BluetoothEnable);
                checkPairedDevices();
            } else {
                startBluetoothIntent();
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
        if (requestCode == REQUEST_ENABLE_BLUETOOTH_Fragment) {
            if (resultCode == Activity.RESULT_OK) {
                updateUIButtons(BluetoothEnable);
                checkPairedDevices();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getContext(), "Bluetooth is not Enable", Toast.LENGTH_LONG).show();
                /*TODO DISABLE ALL BUTTONS*/
            }
        }
    }

    /**
     * Checking if the devices were paired
     */
    private void checkPairedDevices() {
        myDevice = bluetoothState.getBluetoothDevice();
        if (myDevice != null) {
            String deviceName = myDevice.getName();
            //String deviceAddress = myDevice.getAddress();
            bluetoothTextView.setText(deviceName.toUpperCase());
            bluetoothTextView.setContentDescription("Pair to " + deviceName);
            //Check Connection on the background
            setConnection(null);
        } else {
            createToast("There are not paired devices");
            /*TODO DISABLE ALL BUTTONS*/
        }
    }

    /**
     * Prepare message to Send and start Connection to Server
     *
     * @param msg a string
     */
    private void setConnection(String msg) {
        startConnection startConnection = new startConnection();
        messageToSend = msg;
        startConnection.execute();
    }

    /**
     * Start Connection to server
     */
    public class startConnection extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                /*Start a new Connection*/
                myConnection = new BluetoothConnection(myDevice);
                myConnection.start();
                if (messageToSend != null) {
                    //Send a message
                    myConnection.send(messageToSend);
                }
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
                if (messageToSend == null) {
                    updateUIMessage(SUCCESS);
                } else {
                    updateUIMessage(SUCCESS_MSG);
                }
                myConnection.closeSocket();
            } else {
                if (messageToSend == null) {
                    updateUIMessage(FAILED);
                } else {
                    updateUIMessage(FAILED_MSG);
                }
            }
        }
    }


    /**
     * Update UI buttons
     * Enable buttons if bluetooth is enable
     * Disable buttons if bluetooth is not enable
     *
     * @param type a string
     */
    private void updateUIButtons(String type) {
        //Check if user change fragments
        if (destroyed) {
            return;
        }
        switch (type) {
            case BluetoothEnable:
                bluetoothImageView.setImageResource(R.drawable.ic_bluetooth_enabled);
                bluetoothImageView.setContentDescription("Bluetooth is Enable");
                createToast("Bluetooth is Enable");
                break;
        }
    }

    /**
     * Update UI Message after AsyncTask is executed
     *
     * @param connection a string representing the connection
     */
    private void updateUIMessage(String connection) {
        //Check if user change fragments
        if (destroyed) {
            return;
        }
        feedbackTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        switch (connection) {
            case SUCCESS:
                //Update bluetooth logo
                bluetoothImageView.setImageResource(R.drawable.ic_bluetooth_connected);
                bluetoothImageView.setContentDescription("Connected to Guide-Robot");
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destroyed = true;
        if (myConnection != null) {
            myConnection.closeSocket();
        }
    }
}//EOF Class
