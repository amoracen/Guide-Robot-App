package fau.amoracen.guiderobot;


import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Patterns;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

/**
 * Registration Fragment Email Section
 */
public class RegistrationFragment extends Fragment {


    private TextInputEditText emailInputEditText;
    private TextInputLayout emailInputLayout;
    private TextView needHelpTextView;
    private String emailInput;
    //Firebase
    private FirebaseAuth firebaseAuth;

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
        firebaseAuth = FirebaseAuth.getInstance();


        Button buttonNextPage = view.findViewById(R.id.nextPageButton);
        buttonNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateEmail()) {
                    firebaseAuth.fetchSignInMethodsForEmail(emailInput).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            if (task.isSuccessful()) {
                                SignInMethodQueryResult result = task.getResult();
                                List<String> signInMethods = result.getSignInMethods();
                                if (signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                                    emailInputLayout.setErrorEnabled(true);
                                    emailInputLayout.setError("The email has already been used");
                                } else {
                                    emailInputLayout.setErrorEnabled(false);
                                    //Go to Next Page
                                    //Bundle bundle = new Bundle();
                                    //bundle.putString("email", emailInput);
                                    //Navigation.findNavController(view).navigate(R.id.action_registrationFragment_to_FirstLastNameFragment, bundle);
                                }
                            }
                        }
                    });
                }
                /*TODO FAST TESTING*/
                Bundle bundle = new Bundle();
                bundle.putString("email", "test@gmail.com");
                Navigation.findNavController(view).navigate(R.id.action_registrationFragment_to_FirstLastNameFragment, bundle);
            }
        });

        emailInputLayout = view.findViewById(R.id.emailTextInputLayout);
        emailInputEditText = view.findViewById(R.id.emailInputEditText);

        emailInputEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    //EditText has focus
                    //sendAccessibilityEvent("Keyboard is Visible");
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
     * Check Email
     *
     * @return true if a valid email was entered, false otherwise
     */
    private boolean validateEmail() {
        emailInput = emailInputLayout.getEditText().getText().toString().trim();
        emailInputLayout.setErrorEnabled(true);
        if (emailInput.isEmpty()) {
            emailInputLayout.setError("Field for email can't be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            emailInputLayout.setError("Please enter a valid email address");
            return false;
        } else {
            emailInputLayout.setErrorEnabled(false);
            return true;
        }
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
        sendAccessibilityEvent("Registration Form 1-4");
    }
}
