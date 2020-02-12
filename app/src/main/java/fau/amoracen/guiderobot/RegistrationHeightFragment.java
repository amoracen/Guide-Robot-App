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
 * Height Section
 */
public class RegistrationHeightFragment extends Fragment {


    private TextInputEditText heightFeetEditText;
    private TextInputLayout heightFeetLayout;
    private TextInputEditText heightInchesEditText;
    private TextInputLayout heightInchesLayout;
    private TextView needHelpTextView;
    private String feetInput;
    private String inchesInput;
    private Bundle bundle;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reg_height, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*Get email,first and last Name information*/
        bundle = getArguments();
        //Toast.makeText(getContext(), bundle.toString(), Toast.LENGTH_SHORT).show();

        /*Height in feet*/
        heightFeetLayout = view.findViewById(R.id.feetTextInputLayout);
        heightFeetEditText = view.findViewById(R.id.feetInputEditText);
        /*Height in INCHES*/
        heightInchesLayout = view.findViewById(R.id.inchesTextInputLayout);
        heightInchesEditText = view.findViewById(R.id.inchesInputEditText);

        Button buttonNextPage = view.findViewById(R.id.nextPageButton);
        buttonNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateHeight()) {
                    System.out.println("ok");
                    //bundle.putString("heightFeet", feetInput);
                    //bundle.putString("heightInches", inchesInput);
                    // Navigation.findNavController(view).navigate(R.id.action_registrationHeightFragment_to_PasswordFragment, bundle);
                }
                /*TODO FAST TESTING*/
                bundle.putString("heightFeet", "5");
                bundle.putString("heightInches", "10");
                //Toast.makeText(getContext(), bundle.toString(), Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigate(R.id.action_registrationHeightFragment_to_PasswordFragment, bundle);
            }
        });
        heightInchesEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(view);
                    Toast.makeText(getContext(), "Keyboard is not Visible", Toast.LENGTH_SHORT).show();
                }
            }
        });
        needHelpTextView = view.findViewById(R.id.needHelpTextView);
        /*TODO*/
        needHelpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAccessibilityEvent("Not Ready");
            }
        });
    }

    /**
     * Check Height.
     *
     * @return true if height is valid
     */
    public boolean validateHeight() {
        /*Checking Height Feet*/
        feetInput = heightFeetLayout.getEditText().getText().toString().trim();
        heightFeetLayout.setErrorEnabled(true);
        if (feetInput.isEmpty()) {
            heightFeetLayout.setError("Field for feet can't be empty");
            return false;
        }
        int feet = Integer.valueOf(feetInput);
        if (feet < 3 || feet > 8) {
            heightFeetLayout.setError("Please check the height in feet");
            return false;
        } else {
            heightFeetLayout.setErrorEnabled(false);
        }
        /*Checking Height Inches*/
        inchesInput = heightInchesLayout.getEditText().getText().toString().trim();
        heightInchesLayout.setErrorEnabled(true);
        if (inchesInput.isEmpty()) {
            heightInchesLayout.setError("Field for inches can't be empty");
            return false;
        }
        int inches = Integer.valueOf(inchesInput);
        if (inches > 11) {
            heightInchesLayout.setError("Please check the height in inches");
            return false;
        } else {
            heightInchesLayout.setErrorEnabled(false);
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
        sendAccessibilityEvent("Height Form 3-4");
    }

}
