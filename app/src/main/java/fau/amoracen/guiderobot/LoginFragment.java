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
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.regex.Pattern;

/**
 * Login Fragment. Email and Password validation
 */
public class LoginFragment extends Fragment {

    private TextInputEditText emailInputEditText;
    private TextInputLayout emailInputLayout;
    private TextInputEditText passwordEditText;
    private TextInputLayout passwordLayout;
    private TextView needHelpTextView;
    private String emailInput;
    private String passwordInput;
    private FirebaseAuth fireBaseAuth;


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
        fireBaseAuth = FirebaseAuth.getInstance();

        Button buttonNextPage = view.findViewById(R.id.loginButton);
        buttonNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateEmail()) {
                    //Show Error
                    return;
                }
                if(validatePassword()){


                    fireBaseAuth.signInWithEmailAndPassword(emailInput, passwordInput).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //Go to next Page
                                Navigation.findNavController(view).navigate(R.id.action_LoginFragment_to_DashboardActivity, null);
                            } else {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthInvalidUserException e) {
                                    emailInputLayout.setError("Invalid Email");
                                    emailInputLayout.requestFocus();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    passwordLayout.setError("Invalid Password");
                                    passwordLayout.requestFocus();
                                } catch (FirebaseNetworkException e) {
                                    Toast.makeText(getContext(),"Failed to login.No Network available",Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Log.e("Failed", e.getMessage());
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
        emailInputEditText = view.findViewById(R.id.emailInputEditText);
        /*Password*/
        passwordLayout = view.findViewById(R.id.passwordTextInputLayout);
        passwordEditText = view.findViewById(R.id.passwordInputEditText);

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
     * Check Password.
     *
     * @return true if password is valid
     */
    public boolean validatePassword() {
        Pattern anyLetter = Pattern.compile("(?=.*[a-zA-Z])");
        Pattern oneDigit = Pattern.compile("(?=.*[0-9])");
        Pattern noWhiteSpace = Pattern.compile("\\s");
        Pattern totalCharacters = Pattern.compile(".{6,}");

        /*Checking Password*/
        passwordInput = passwordLayout.getEditText().getText().toString().trim();
        passwordLayout.setErrorEnabled(true);
        if (passwordInput.isEmpty()) {
            passwordLayout.setError("Field for password can't be empty");
            return false;
        } else if (!anyLetter.matcher(passwordInput).find()) {
            passwordLayout.setError("Password must include a letter");
            return false;
        } else if (!oneDigit.matcher(passwordInput).find()) {
            passwordLayout.setError("Password must include at least one digit");
            return false;
        } else if (noWhiteSpace.matcher(passwordInput).find()) {
            passwordLayout.setError("No white spaces allowed");
            return false;
        } else if (!totalCharacters.matcher(passwordInput).find()) {
            passwordLayout.setError("Password must be at least 6 characters long");
            return false;
        }
        passwordLayout.setErrorEnabled(false);
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
        sendAccessibilityEvent("Login Screen");
    }
}