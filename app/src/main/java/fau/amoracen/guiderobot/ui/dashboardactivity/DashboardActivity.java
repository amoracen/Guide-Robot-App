package fau.amoracen.guiderobot.ui.dashboardactivity;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import fau.amoracen.guiderobot.R;
import fau.amoracen.guiderobot.service.Firebase;
import fau.amoracen.guiderobot.ui.mainactivity.MainActivity;

/**
 * Dashboard displays the bottom navigation and updates fragments
 */
public class DashboardActivity extends AppCompatActivity implements DashboardFragment.pairDeviceListener {

    private boolean doubleBackToExitPressedOnce = false;
    private Firebase myFirebase;
    private boolean pairFragment = false;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 1;

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
                            selectedFragment = new PairDevicesFragment();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        //Firebase
        myFirebase = new Firebase();
        //Reference to bottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
    }//EOF onCreate


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

    @Override
    public void onResultOpenPairDevice() {
        //Create a new Pair Fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PairDevicesFragment()).commit();
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
