package fau.amoracen.guiderobot;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * First and Last Name Section.
 */
public class FirstLastNameFragment extends Fragment {


    private TextInputLayout firstNameLayout;
    private TextInputLayout lastNameLayout;
    private Bundle bundle;
    private String firstInput;
    private String lastInput;

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
        TextInputEditText firstNameEditText = view.findViewById(R.id.firstNameEditText);
        /*Last Name Information*/
        lastNameLayout = view.findViewById(R.id.lastNameTextInputLayout);
        TextInputEditText lastNameEditText = view.findViewById(R.id.lastNameEditText);


        Button buttonNextPage = view.findViewById(R.id.nextPageButton);
        buttonNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateName()) {
                    //Go to Next Page
                    bundle.putString("firstName", firstInput);
                    bundle.putString("lastName", lastInput);
                    Navigation.findNavController(view).navigate(R.id.action_firstLastNameFragment_to_RegistrationHeightFragment, bundle);
                }
                /*TODO FAST TESTING*/
                //bundle.putString("firstName", "Alex");
                //bundle.putString("lastName", "Last");
                //Navigation.findNavController(view).navigate(R.id.action_firstLastNameFragment_to_RegistrationHeightFragment, bundle);
            }
        });


        lastNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(view);
                    Toast.makeText(getContext(), "Keyboard is not Visible", Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView needHelpTextView = view.findViewById(R.id.needHelpTextView);
        /*TODO*/
        needHelpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAccessibilityEvent("Not Ready");
            }
        });
    }

    /**
     * Check first and last name.
     * TODO Refactor Validation
     * @return true if first and last name are valid
     */
    public boolean validateName() {
        /*Checking First Name*/
        firstInput = firstNameLayout.getEditText().getText().toString().trim();
        firstNameLayout.setErrorEnabled(true);
        if (firstInput.isEmpty()) {
            firstNameLayout.setError("Field for first name can't be empty");
            return false;
        } else if (firstInput.length() > 25) {
            firstNameLayout.setError("First name too long.No more than 25 characters");
            return false;
        } else {
            firstNameLayout.setErrorEnabled(false);
        }
        /*Checking Last Name*/
        lastInput = lastNameLayout.getEditText().getText().toString().trim();
        lastNameLayout.setErrorEnabled(true);
        if (lastInput.isEmpty()) {
            lastNameLayout.setError("Field for last name can't be empty");
            return false;
        } else if (lastInput.length() > 25) {
            lastNameLayout.setError("Last name too long.No more than 25 characters");
            return false;
        } else {
            lastNameLayout.setErrorEnabled(false);
        }
        return true;
    }

    /**
     * Hide Keyboard form View
     *
     * @param view current View
     */
    private void hideKeyboard(View view) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Wait for Voice Assistance and send an accessibility event
     *
     * @param msg a string
     */
    public void sendAccessibilityEvent(final String msg) {
        //Wait
        new CountDownTimer(400, 1000) {
            public void onTick(long millisecondsUntilDone) {
                //countdown every second
            }

            public void onFinish() {
                //Counter is finished
                Log.i("Done ", "CountDown Finished");
                try {
                    AccessibilityManager manager = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
                    if (manager != null && manager.isEnabled()) {
                        AccessibilityEvent e = AccessibilityEvent.obtain();
                        e.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                        e.setClassName(getClass().getName());
                        e.setPackageName(getContext().getPackageName());
                        e.getText().add(msg);
                        manager.sendAccessibilityEvent(e);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        sendAccessibilityEvent("First and Last Name Form 2-4");
    }
}
