package fau.amoracen.guiderobot;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import java.util.List;
import java.util.Locale;

/**
 * Class to implement text to Speech
 */
class TextToSpeechService {
    private boolean ttsIsInitialized = false;
    private boolean screenReader;
    private TextToSpeech textToSpeech;

    TextToSpeechService(Context context) {
        //Check if screen reader is on
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager != null && accessibilityManager.isEnabled()) {
            //If there are Accessibility services enabled
            List<AccessibilityServiceInfo> serviceInfo = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN);
            if (!serviceInfo.isEmpty()) {
                //Toast.makeText(this,"ACCESSIBILITY_SERVICE is Enable",Toast.LENGTH_LONG).show();
                screenReader = true;
            }
        } else {
            screenReader = false;
        }
        if (!screenReader) {
            // initialize the tts here once,
            textToSpeech = new android.speech.tts.TextToSpeech(context, new android.speech.tts.TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                        int result = textToSpeech.setLanguage(Locale.ENGLISH);
                        if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA || result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("TTS", "Language not supported");
                        } else {
                            ttsIsInitialized = true; // flag tts as initialized
                        }
                    } else {
                        Log.e("TTS", "Failed");
                    }
                }
            });
        }
    }

    /**
     * Reads a string to the user
     *
     * @param text a string representing what to read to the user
     */
    void speak(String text) {
        if (!ttsIsInitialized) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    void stopTextToSpeech() {
        if (textToSpeech == null) return;
        textToSpeech.stop();
        textToSpeech.shutdown();
    }

    boolean isScreenReader() {
        return screenReader;
    }
}
