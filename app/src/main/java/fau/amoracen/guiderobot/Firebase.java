package fau.amoracen.guiderobot;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Objects;

/**
 * A class to manage Firebase
 */
class Firebase {
    private FirebaseAuth myfirebaseAuth;
    private boolean emailFound;
    private DatabaseReference myRef;

    /**
     * Constructor
     */
    Firebase() {
        myfirebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Get firebase user
     *
     * @return FirebaseAuth
     */
    FirebaseAuth getMyFirebaseAuth() {
        return myfirebaseAuth;
    }

    /**
     * Get current user
     * Null if the user does not exist
     *
     * @return an instance of the current user
     */
    FirebaseUser getCurrentUser() {
        return myfirebaseAuth.getCurrentUser();
    }

    /**
     * Checking the database trying to find an email
     *
     * @param emailInput a string representing the user's input
     */
    void checkEmailOnDatabase(String emailInput) {
        setEmailFound(true);
        myfirebaseAuth.fetchSignInMethodsForEmail(emailInput).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {
                    SignInMethodQueryResult result = task.getResult();
                    assert result != null;
                    List<String> signInMethods = result.getSignInMethods();
                    assert signInMethods != null;
                    if (!signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                        setEmailFound(false);
                    }
                }
            }
        });
    }

    /**
     * Set emailFound to true if the email was already used on the database
     *
     * @param emailFound a boolean
     */
    private void setEmailFound(boolean emailFound) {
        this.emailFound = emailFound;
    }

    /**
     * Get emailFound, true if email was used on the database
     *
     * @return a boolean
     */
    boolean getEmailFound() {
        return emailFound;
    }

    /**
     * Create firebase database and path
     *
     * @param path a string
     */
    void setFireBaseDatabase(String path) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.myRef = database.getReference(path);
    }

    /**
     * Save a User to the database
     *
     * @param userInfo an instance of the UserInfo class
     */
    void insertUser(UserInfo userInfo) {
        myRef.child(Objects.requireNonNull(myfirebaseAuth.getUid())).setValue(userInfo);
    }
}
