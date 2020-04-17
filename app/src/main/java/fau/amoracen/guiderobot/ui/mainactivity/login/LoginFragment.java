package fau.amoracen.guiderobot.ui.mainactivity.login;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import fau.amoracen.guiderobot.R;
import fau.amoracen.guiderobot.service.Firebase;
import fau.amoracen.guiderobot.service.MyAccessibilityEvent;
import fau.amoracen.guiderobot.service.SpeechToTextService;
import fau.amoracen.guiderobot.service.ValidateInput;
import fau.amoracen.guiderobot.ui.dashboardactivity.DashboardActivity;

/**
 * Login Fragment. Email and Password validation
 */
public class LoginFragment extends Fragment {

    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordLayout;
    private String emailInput;
    private String passwordInput;
    //Firebase
    private Firebase myFirebase;
    private FloatingActionButton micButton;
    private SpeechToTextService speechToTextService;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Firebase
        myFirebase = new Firebase();
        //Microphone
        micButton = view.findViewById(R.id.micButton);
        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechToTextService.startListening(getActivity());
            }
        });
        initializeSpeechRecognizer();

        Button buttonNextPage = view.findViewById(R.id.loginButton);
        buttonNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateEmail() && validatePassword()) {
                    myFirebase.getMyFirebaseAuth().signInWithEmailAndPassword(emailInput, passwordInput).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //Go to Dashboard
                                Intent goToDashboard = new Intent(getContext(), DashboardActivity.class);
                                startActivity(goToDashboard);
                                Objects.requireNonNull(getActivity()).finishAffinity();
                            } else {
                                try {
                                    throw Objects.requireNonNull(task.getException());
                                } catch (FirebaseAuthInvalidUserException e) {
                                    emailInputLayout.setError("Invalid Email");
                                    emailInputLayout.requestFocus();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    passwordLayout.setError("Invalid Password");
                                    passwordLayout.requestFocus();
                                } catch (FirebaseNetworkException e) {
                                    Toast.makeText(getContext(), "Failed to login.No Network available", Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Log.e("Failed", Objects.requireNonNull(e.getMessage()));
                                }
                                Log.w("Failed", "signInWithEmail:failed", task.getException());
                            }
                        }
                    });


                }
            }
        });
        /*Email*/
        emailInputLayout = view.findViewById(R.id.emailTextInputLayout);
        /*Password*/
        passwordLayout = view.findViewById(R.id.passwordTextInputLayout);
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
     * Check Password.
     *
     * @return true if password is valid
     */
    private boolean validatePassword() {
        passwordInput = Objects.requireNonNull(passwordLayout.getEditText()).getText().toString().trim();
        HashMap hmap = ValidateInput.getInstance().validatePassword(getContext(), passwordInput);
        if (!Objects.equals(hmap.get("message"), "Valid")) {
            passwordLayout.setErrorEnabled(true);
            passwordLayout.setError(Objects.requireNonNull(hmap.get("message")).toString());
            return false;
        }
        passwordLayout.setErrorEnabled(false);
        return true;
    }


    @Override
    public void onResume() {
        super.onResume();
        /*Start an Accessibility Event*/
        MyAccessibilityEvent.getInstance().sendAccessibilityEvent(getContext(), "Login Screen");
    }
}