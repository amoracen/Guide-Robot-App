package fau.amoracen.guiderobot.ui.mainactivity.registration;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import fau.amoracen.guiderobot.R;
import fau.amoracen.guiderobot.service.Firebase;
import fau.amoracen.guiderobot.service.MyAccessibilityEvent;
import fau.amoracen.guiderobot.service.SpeechToTextService;
import fau.amoracen.guiderobot.service.ValidateInput;

/**
 * Registration Fragment Email Section
 */
public class RegistrationEmailFragment extends Fragment {


    private TextInputLayout emailInputLayout;
    private String emailInput;
    //Firebase
    private Firebase myFirebase;
    private FloatingActionButton micButton;
    private SpeechToTextService speechToTextService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reg_email, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*Initialize Firebase*/
        myFirebase = new Firebase();
        /*Initialize UI widgets*/
        emailInputLayout = view.findViewById(R.id.emailTextInputLayout);
        micButton = view.findViewById(R.id.micButton);
        Button buttonNextPage = view.findViewById(R.id.nextPageButton);

        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechToTextService.startListening(getActivity());
            }
        });
        initializeSpeechRecognizer();


        buttonNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateEmail()) {
                    myFirebase.checkEmailOnDatabase(emailInput);
                    //Wait 1 seconds
                    new CountDownTimer(1000, 1000) {
                        public void onTick(long millisecondsUntilDone) {
                            //countdown every second
                        }

                        public void onFinish() {
                            //Counter is finished(after 1 seconds)
                            Log.i("Done ", "CountDown Finished");
                            if (myFirebase.getEmailFound()) {
                                emailInputLayout.setErrorEnabled(true);
                                emailInputLayout.setError("The email has already been used");
                            } else {
                                emailInputLayout.setErrorEnabled(false);
                                //Go to Next Page
                                Bundle bundle = new Bundle();
                                bundle.putString("email", emailInput);
                                Navigation.findNavController(view).navigate(R.id.action_registrationFragment_to_FirstLastNameFragment, bundle);
                            }
                        }
                    }.start();
                }
            }
        });
    }

    /**
     * Check Email
     *
     * @return true if a valid email was entered, false otherwise
     */
    private boolean validateEmail() {
        emailInput = Objects.requireNonNull(emailInputLayout.getEditText()).getText().toString().trim();
        HashMap hmap = ValidateInput.getInstance().validateEmail(getContext(), emailInput);
        if (!Objects.equals(hmap.get("message"), "Valid")) {
            emailInputLayout.setErrorEnabled(true);
            emailInputLayout.setError(Objects.requireNonNull(hmap.get("message")).toString());
            return false;
        }
        emailInputLayout.setErrorEnabled(false);
        return true;
    }

    /**
     * Initialize the Speech Recognition Listener
     */
    private void initializeSpeechRecognizer() {
        speechToTextService = new SpeechToTextService(getContext());
        if (!speechToTextService.isRecognitionAvailable()) {
            micButton.setImageResource(R.drawable.ic_mic_off_blue_75dp);
            micButton.setContentDescription(getString(R.string.mic_description_email_off));
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
                processEmail(result_arr.get(0));
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
     * Cleaning the input before validation
     *
     * @param input a string
     */
    private void processEmail(String input) {
        boolean atFound = false;
        String[] temp = input.split(" ");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < temp.length; i++) {
            Log.i("INPUT", temp[i]);
            if (!atFound && temp[i].contains("at")) {
                temp[i] = "@";
                atFound = true;
            }
            result.append(temp[i].toLowerCase().trim());
        }
        Objects.requireNonNull(emailInputLayout.getEditText()).setText(result);
        validateEmail();
    }


    @Override
    public void onResume() {
        super.onResume();
        /*Start an Accessibility Event*/
        MyAccessibilityEvent.getInstance().sendAccessibilityEvent(getContext(), "Registration Form.Email Screen");
    }
}
