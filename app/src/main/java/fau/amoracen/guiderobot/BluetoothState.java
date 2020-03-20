package fau.amoracen.guiderobot;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Set;

/**
 * Class to manage bluetooth adapter
 */
public class BluetoothState {
    private BluetoothAdapter bluetoothAdapter;
    private static BluetoothState instance;

    /**
     * Constructor
     *
     * @param adapter an instance of BluetoothAdapter
     */
    private BluetoothState(BluetoothAdapter adapter) {
        this.bluetoothAdapter = adapter;
    }

    /**
     * Get instance
     *
     * @param adapter an instance of BluetoothAdapter
     * @return instance of BluetoothState
     */
    public static synchronized BluetoothState getInstance(BluetoothAdapter adapter) {
        if (instance == null) {
            instance = new BluetoothState(adapter);
        }
        return instance;
    }

    /**
     * Checking if Bluetooth is Enabled
     */
    boolean checkBluetoothState() {
        return bluetoothAdapter.isEnabled();
    }

    /**
     * Check if bluetooth is supported
     *
     * @return a boolean, true if bluetooth is supported
     */
    boolean BluetoothSupported() {
        return bluetoothAdapter != null;
    }

    /**
     * Checking all paired devices
     *
     * @return a Bluetooth device, null if the device is not found
     */
    BluetoothDevice getBluetoothDevice() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if ("guide-robot".equals(device.getName())) {
                    return device;
                }
            }
        }
        return null;
    }
}
