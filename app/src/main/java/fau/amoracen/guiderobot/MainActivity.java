package fau.amoracen.guiderobot;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Locale;

/**
 * MainActivity displays two main options to the user, Create Account and Login
 * MainActivity checks if screen reader is enabled
 */
public class MainActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private boolean ttsIsInitialized = false;
    private boolean screenReader;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        NavController navController = Navigation.findNavController(this, R.id.my_nav_host_fragment);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

        //Check if screen reader is on
        AccessibilityManager accessibilityManager = (AccessibilityManager) getApplicationContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager != null && accessibilityManager.isEnabled()) {
            //If there are Accessibility services enabled
            List<AccessibilityServiceInfo> serviceInfo = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN);
            if (!serviceInfo.isEmpty()) {
                //Toast.makeText(this,"ACCESSIBILITY_SERVICE is Enable",Toast.LENGTH_LONG).show();
                screenReader = true;
            }
        } else {
            screenReader = false;
            //If no Accessibility service is enabled
            //Intent goToSettings = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            //startActivity(goToSettings);
        }
        if (!screenReader) {
            // initialize the tts here once,
            textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = textToSpeech.setLanguage(Locale.ENGLISH);
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("TTS", "Language not supported");
                        } else {
                            ttsIsInitialized = true; // flag tts as initialized
                        }
                    } else {
                        Log.e("TTS", "Failed");
                    }
                }
            });
            //Wait 1 seconds for TextToSpeech
            new CountDownTimer(1000, 1000) {
                public void onTick(long millisecondsUntilDone) {
                    //countdown every second
                }

                public void onFinish() {
                    //Counter is finished(after 1 seconds)
                    Log.i("Done ", "CountDown Finished");
                    speak("Voice Assistance is not Enable.Please, Go to Settings and enable Voice Assistance.");
                }
            }.start();
            Toast.makeText(this, "Voice Assistance is not Enable", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Reads a string to the user
     *
     * @param text a string representing what to read to the user
     */
    public void speak(String text) {
        if (!ttsIsInitialized) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null){
            Intent goToDashboard = new Intent(getApplicationContext(), DashboardActivity.class);
            startActivity(goToDashboard);
            finish();
        }
    }
    /*Close TextToSpeech*/
    @Override
    protected void onDestroy(){
        if(textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}