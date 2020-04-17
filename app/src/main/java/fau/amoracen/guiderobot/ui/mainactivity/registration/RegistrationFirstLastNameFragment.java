package fau.amoracen.guiderobot.ui.mainactivity.registration;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import fau.amoracen.guiderobot.R;
import fau.amoracen.guiderobot.service.MyAccessibilityEvent;
import fau.amoracen.guiderobot.service.SpeechToTextService;
import fau.amoracen.guiderobot.service.ValidateInput;

/**
 * First and Last Name Section.
 */
public class RegistrationFirstLastNameFragment extends Fragment {


    private TextInputLayout firstNameLayout;
    private TextInputEditText firstNameEditText;
    private TextInputLayout lastNameLayout;
    private TextInputEditText lastNameEditText;
    private Bundle bundle;
    private String firstInput;
    private String lastInput;
    private FloatingActionButton micButton;
    private SpeechToTextService speechToTextService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reg_first_last_name, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*Get email information*/
        bundle = getArguments();

        /*First Name Information*/
        firstNameLayout = view.findViewById(R.id.firstNameTextInputLayout);
        firstNameEditText = view.findViewById(R.id.firstNameEditText);
        /*Last Name Information*/
        lastNameLayout = view.findViewById(R.id.lastNameTextInputLayout);
        lastNameEditText = view.findViewById(R.id.lastNameEditText);

        micButton = view.findViewById(R.id.micButton);
        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechToTextService.startListening(getActivity());
            }
        });
        initializeSpeechRecognizer();

        Button buttonNextPage = view.findViewById(R.id.nextPageButton);
        buttonNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateName()) {
                    //Go to Next Page
                    bundle.putString("firstName", firstInput);
                    bundle.putString("lastName", lastInput);
                    Navigation.findNavController(view).navigate(R.id.action_firstLastNameFragment_to_RegistrationPassword, bundle);
                }
            }
        });
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
                processFullName(result_arr.get(0));
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
    private void processFullName(String input) {
        String[] temp = input.split(" ");
        firstNameEditText.setText(" ");
        lastNameEditText.setText(" ");
        if (temp.length >= 2) {
            Objects.requireNonNull(firstNameLayout.getEditText()).setText(temp[0].toLowerCase());
            Objects.requireNonNull(lastNameLayout.getEditText()).setText(temp[1].toLowerCase());
        } else if (temp.length == 1) {
            Objects.requireNonNull(firstNameLayout.getEditText()).setText(temp[0].toLowerCase());
        }
        validateName();
    }

    /**
     * Check first and last name.
     *
     * @return true if first and last name are valid
     */
    private boolean validateName() {
        /*Checking First Name*/
        firstInput = Objects.requireNonNull(firstNameLayout.getEditText()).getText().toString().trim();
        HashMap hmap = ValidateInput.getInstance().validateName(getContext(), firstInput);
        if (!Objects.equals(hmap.get("message"), "Valid")) {
            firstNameLayout.setErrorEnabled(true);
            firstNameLayout.setError(Objects.requireNonNull(hmap.get("message")).toString());
            return false;
        }
        firstNameLayout.setErrorEnabled(false);

        /*Checking Last Name*/
        lastInput = Objects.requireNonNull(lastNameLayout.getEditText()).getText().toString().trim();
        hmap = ValidateInput.getInstance().validateName(getContext(), lastInput);
        if (!Objects.equals(hmap.get("message"), "Valid")) {
            lastNameLayout.setErrorEnabled(true);
            lastNameLayout.setError(Objects.requireNonNull(hmap.get("message")).toString());
            return false;
        }
        lastNameLayout.setErrorEnabled(false);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        /*Start an Accessibility Event*/
        MyAccessibilityEvent.getInstance().sendAccessibilityEvent(getContext(),"Registration Form.First and Last Name Screen");
    }
}
