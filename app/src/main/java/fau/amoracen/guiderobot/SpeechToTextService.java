package fau.amoracen.guiderobot;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Objects;

class SpeechToTextService {
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private SpeechRecognizer speechRecognizer;
    private Context context;
    private boolean isRecognitionAvailable = false;

    SpeechToTextService(Context context) {
        this.context = context;
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            isRecognitionAvailable = true;
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED;
    }


    void startListening(Activity activity) {
        if (checkPermission()) {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(Objects.requireNonNull(activity), new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            // Permission has already been granted
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            if (speechRecognizer != null) {
                speechRecognizer.startListening(intent);
            }
        }
    }

    SpeechRecognizer getSpeechRecognizer() {
        return speechRecognizer;
    }

    boolean isRecognitionAvailable() {
        return isRecognitionAvailable;
    }
}//EOF Class
