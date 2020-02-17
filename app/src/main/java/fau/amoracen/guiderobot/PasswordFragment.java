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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Password Section
 */
public class PasswordFragment extends Fragment {

    private TextInputLayout passwordLayout;
    private String passwordInput;
    private Bundle bundle;
    private FirebaseAuth fireBaseAuth;
    //Database
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*Firebase*/
        fireBaseAuth = FirebaseAuth.getInstance();
        //Write to database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users/");
        /*Get email,first name, last name, and height information*/
        bundle = getArguments();


        /*Password*/
        passwordLayout = view.findViewById(R.id.passwordTextInputLayout);
        TextInputEditText passwordEditText = view.findViewById(R.id.passwordInputEditText);

        Button buttonSubmit = view.findViewById(R.id.registerButton);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validatePassword()) {
                    bundle.putString("password", passwordInput);
                    if (bundle.getString("email") != null && bundle.getString("password") != null) {
                        fireBaseAuth.createUserWithEmailAndPassword(bundle.getString("email"), bundle.getString("password")).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    /*Account Created*/
                                    UserInfo userInfo = new UserInfo(bundle.getString("firstName"), bundle.getString("lastName"), bundle.getString("heightFeet"), bundle.getString("heightInches"));
                                    //Save to database
                                    myRef.child(Objects.requireNonNull(fireBaseAuth.getUid())).setValue(userInfo);
                                    Navigation.findNavController(view).navigate(R.id.action_passwordFragment_to_dashboardActivity, null);
                                } else {
                                    Toast.makeText(getContext(), "Sign Up Unsuccessful.Please Try Again", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            }
        });
        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(view);
                    Toast.makeText(getContext(), "Keyboard is not Visible", Toast.LENGTH_SHORT).show();
                } else {
                    passwordLayout.setErrorEnabled(false);
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
        sendAccessibilityEvent("Password Form 4-4");
    }
}
