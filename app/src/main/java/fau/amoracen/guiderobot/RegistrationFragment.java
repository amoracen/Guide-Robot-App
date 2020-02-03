package fau.amoracen.guiderobot;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Registration Fragment Email Section
 */
public class RegistrationFragment extends Fragment {


    private EditText emailEditText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonNextPage = view.findViewById(R.id.nextPageButton);
        buttonNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_registrationFragment_to_FirstLastNameFragment, null);
            }
        });

        emailEditText = view.findViewById(R.id.emailEditText);
        emailEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    sendAccessibilityEvent("Keyboard is Visible");
                } else {
                    hideKeyboard(view);
                    Toast.makeText(getContext(), "Keyboard is not Visible", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    /**
     * Hide Keyboard form View
     *
     * @param view current View
     */
    public void hideKeyboard(View view) {
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
