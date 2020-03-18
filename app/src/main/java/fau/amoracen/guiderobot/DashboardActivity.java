package fau.amoracen.guiderobot;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Set;

/**
 * Dashboard displays the bottom navigation and updates fragments
 */
public class DashboardActivity extends AppCompatActivity {


    private boolean doubleBackToExitPressedOnce = false;
    private FirebaseAuth firebaseAuth;
    private boolean pairFragment = false;
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BLUETOOTH = 11;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        //Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        //Reference to bottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        //Get bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //checkPairedDevices();
        checkBluetooth checkBluetooth = new checkBluetooth();
        checkBluetooth.execute();
    }//EOF onCreate

    /**
     * Navigate between Fragments
     */
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.nav_dashboard:
                            selectedFragment = new DashboardFragment();
                            pairFragment = false;
                            break;
                        case R.id.nav_bluetooth:
                            selectedFragment = new BluetoothFragment();
                            pairFragment = true;
                            break;
                        case R.id.nav_setting:
                            selectedFragment = new SettingFragment();
                            pairFragment = false;
                            break;
                    }
                    assert selectedFragment != null;
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                    return true;
                }
            };


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            //Do Nothing to disable back button
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

    @Override
    protected void onStart() {
        super.onStart();
        //Check if user is log in
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Intent goToMainActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(goToMainActivity);
            finish();
        }
    }

    /**
     * Start Connection to server
     */
    public class checkBluetooth extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            checkBluetoothState();
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    /**
     * Checking if Bluetooth is Enabled
     */
    private void checkBluetoothState() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on your device!", Toast.LENGTH_SHORT).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                //Toast.makeText(getActivity(), "You Need to Enable Bluetooth", Toast.LENGTH_SHORT).show();
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
            } else {
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
            checkPairedDevices();
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Bluetooth is Off", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Checking if the devices were paired
     * TODO implement doInBackground
     */
    public void checkPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        boolean deviceFound = false;
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                Log.i("Devices Paired", device.getName());
                if ("guide-robot".equals(device.getName())) {
                    deviceFound = true;
                    //Create a new Dashboard Fragment
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
                }
            }
        }
        if (!deviceFound) {
            Toast.makeText(this, "There are not paired devices", Toast.LENGTH_SHORT).show();
            pairFragment = true;
            //Create a new Bluetooth Fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BluetoothFragment()).commit();
        }
    }


    /**
     * After checkCoarseLocationPermission's result is returned
     *
     * @param requestCode  integer
     * @param permissions  integer
     * @param grantResults int
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION:
                if (!pairFragment) {
                    break;
                }
                Button scanningButton = findViewById(R.id.scanningBtn);
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    scanningButton.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Access coarse location allowed. You can scan for bluetooth devices", Toast.LENGTH_SHORT).show();
                } else {
                    scanningButton.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "Access coarse location not allowed. You can not scan for bluetooth devices", Toast.LENGTH_SHORT).show();

                }
                break;
        }
    }
}//EOF CLASS
