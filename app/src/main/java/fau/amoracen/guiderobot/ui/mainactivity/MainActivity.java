package fau.amoracen.guiderobot.ui.mainactivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import fau.amoracen.guiderobot.R;
import fau.amoracen.guiderobot.service.Firebase;
import fau.amoracen.guiderobot.service.TextToSpeechService;
import fau.amoracen.guiderobot.ui.dashboardactivity.DashboardActivity;

/**
 * MainActivity displays two main options to the user, Create Account and Login
 * MainActivity checks if screen reader is enabled
 */
public class MainActivity extends AppCompatActivity {
    private TextToSpeechService textToSpeechService;
    private Firebase myFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Firebase
        myFirebase = new Firebase();

        NavController navController = Navigation.findNavController(this, R.id.my_nav_host_fragment);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        //Initialize TextToSpeech
        textToSpeechService = new TextToSpeechService(getApplicationContext());
    }

    /**
     * Check if Voice Assistance is turn on
     */
    public void checkVoiceAssistance() {
        if (textToSpeechService.isScreenReader()) return;
        //Wait 1 seconds for TextToSpeech
        new CountDownTimer(1000, 1000) {
            public void onTick(long millisecondsUntilDone) {
                //countdown every second
            }

            public void onFinish() {
                //Counter is finished(after 1 seconds)
                Log.i("Done ", "CountDown Finished");
                textToSpeechService.speak("Voice Assistance is not Enable.Please, Go to Settings and enable Voice Assistance.");
            }
        }.start();
        Toast.makeText(this, "Voice Assistance is not Enable", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (myFirebase.getCurrentUser() != null) {
            //If the user is logged in, go to Dashboard
            Intent goToDashboard = new Intent(getApplicationContext(), DashboardActivity.class);
            startActivity(goToDashboard);
            finish();
        } else {
            checkVoiceAssistance();
        }
    }

    /*Close TextToSpeech*/
    @Override
    protected void onDestroy() {
        textToSpeechService.stopTextToSpeech();
        super.onDestroy();
    }
}