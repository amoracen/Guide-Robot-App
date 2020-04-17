package fau.amoracen.guiderobot.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Start a bluetooth Connection
 */
public class BluetoothConnection extends Thread {

    private BluetoothDevice myDevice;
    private final BluetoothSocket mmSocket;
    // MY_UUID is the app's UUID string, also used in the server code.
    private final String MY_UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee";

    public BluetoothConnection(BluetoothDevice device) throws IOException {
        // Use a temporary object that is later assigned to mmSocket
        BluetoothSocket tmp = null;
        myDevice = device;
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            UUID uuid = UUID.fromString(MY_UUID);
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            Log.e("Fail", "Socket's create() method failed", e);
        }
        mmSocket = tmp;
        mmSocket.connect();
    }

    public void send(String msg) throws IOException {
        OutputStream mmOutputStream = mmSocket.getOutputStream();
        mmOutputStream.write(msg.getBytes());
    }

     public void receive() throws IOException {
            InputStream mmInputStream = mmSocket.getInputStream();
            byte[] buffer = new byte[256];
            int bytes;
            try {
                bytes = mmInputStream.read(buffer);
                String readMessage = new String(buffer, 0, bytes);
                Log.d("Received", "Message: " + readMessage);
                mmSocket.close();
            } catch (IOException e) {
                Log.e("IOException", "Problems occurred!");
            }
        }


    public void closeSocket() {
        try {
            if (mmSocket != null) {
                mmSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
