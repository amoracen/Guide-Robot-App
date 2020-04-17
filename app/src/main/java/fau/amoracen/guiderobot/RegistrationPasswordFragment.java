package fau.amoracen.guiderobot;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;

import java.util.HashMap;
import java.util.Objects;

/**
 * Password Section
 */
public class RegistrationPasswordFragment extends Fragment {

    private TextInputLayout passwordLayout;
    private String passwordInput;
    private Bundle bundle;
    //Firebase
    private Firebase myFirebase;

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
        myFirebase = new Firebase();
        //Set database path
        myFirebase.setFireBaseDatabase("Users/");
        /*Get email,first name, last name, and height information*/
        bundle = getArguments();
        /*Password*/
        passwordLayout = view.findViewById(R.id.passwordTextInputLayout);

        Button buttonSubmit = view.findViewById(R.id.registerButton);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validatePassword()) {
                    if (bundle.getString("email") != null) {
                        myFirebase.getMyFirebaseAuth().createUserWithEmailAndPassword(Objects.requireNonNull(bundle.getString("email")), passwordInput).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    /*Account Created*/
                                    UserInfo userInfo = new UserInfo(bundle.getString("firstName"), bundle.getString("lastName"));
                                    //Save to database
                                    myFirebase.insertUser(userInfo);
                                    //Go to Dashboard
                                    Intent goToDashboard = new Intent(getContext(), DashboardActivity.class);
                                    startActivity(goToDashboard);
                                    Objects.requireNonNull(getActivity()).finishAffinity();
                                    //Navigation.findNavController(view).navigate(R.id.action_passwordFragment_to_dashboardActivity, null);
                                } else {
                                    Toast.makeText(getContext(), "Sign Up Unsuccessful.Please Try Again", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            }
        });
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

    /**
     * Hide Keyboard form View
     *
     * @param view current View
     */
    private void hideKeyboard(View view) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        /*Start an Accessibility Event*/
        MyAccessibilityEvent.getInstance().sendAccessibilityEvent(getContext(), "Registration Form.Password Screen");
    }
}
