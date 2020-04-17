package fau.amoracen.guiderobot;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Dashboard displays the bottom navigation and updates fragments
 */
public class DashboardActivity extends AppCompatActivity {


    private boolean doubleBackToExitPressedOnce = false;
    private Firebase myFirebase;
    private BluetoothState bluetoothState;
    private boolean pairFragment = false;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BLUETOOTH_Activity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        //Firebase
        myFirebase = new Firebase();
        //Reference to bottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        //Get bluetooth adapter
        bluetoothState = BluetoothState.getInstance(BluetoothAdapter.getDefaultAdapter());
        //Start AsyncTask to check bluetooth
        checkBluetoothState checkBluetooth = new checkBluetoothState();
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
        createToast("Please click BACK again to exit");
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
        if (myFirebase.getCurrentUser() == null) {
            Intent goToMainActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(goToMainActivity);
            finish();
        }
    }

    /**
     * Check if bluetooth is supported and enabled
     */
    public class checkBluetoothState extends AsyncTask<Void, Void, Void> {
        private boolean bluetoothSupported = false;
        private boolean bluetoothEnabled = false;

        @Override
        protected Void doInBackground(Void... params) {
            if (bluetoothState.BluetoothSupported()) {
                bluetoothSupported = true;
            } else {
                return null;
            }
            bluetoothEnabled = bluetoothState.checkBluetoothState();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!bluetoothSupported) {
                createToast("Bluetooth is not supported on your device!");
                //Create a new Dashboard Fragment
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
            } else {
                if (bluetoothEnabled) {
                    checkPairedDevices();
                } else {
                    startBluetoothIntent();
                }
            }
        }
    }


    /**
     * Create a Toast message
     *
     * @param message a string
     */
    public void createToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Start Intent to request permission to use Bluetooth
     */
    public void startBluetoothIntent() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH_Activity);
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
        if (requestCode == REQUEST_ENABLE_BLUETOOTH_Activity) {
            if (resultCode == Activity.RESULT_OK) {
                checkPairedDevices();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //createToast("Bluetooth is not Enabled");
                //Create a new Dashboard Fragment
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
            }
        }
    }

    /**
     * Checking if the devices were paired
     */
    public void checkPairedDevices() {
        BluetoothDevice myDevice = bluetoothState.getBluetoothDevice();
        if (myDevice != null) {
            //Create a new Dashboard Fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
        } else {
            createToast("There are not paired devices");
            pairFragment = true;
            //Create a new Pair Fragment
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
        if (requestCode == REQUEST_ACCESS_COARSE_LOCATION) {
            if (!pairFragment) {
                return;
            }
            Button scanningButton = findViewById(R.id.scanningBtn);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanningButton.setEnabled(true);
                createToast("Access coarse location allowed. You can scan for bluetooth devices");
            } else {
                scanningButton.setEnabled(false);
                createToast("Access coarse location not allowed. You can not scan for bluetooth devices");
            }
        }
    }
}//EOF CLASS
