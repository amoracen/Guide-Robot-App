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
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

/**
 * HomeFragment displays the button to Create Account and Login
 */
public class HomeFragment extends Fragment {

    private boolean buttonPressed = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonRegistration = view.findViewById(R.id.createAccountButton);
        buttonRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonPressed = true;
                Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_regitrationFragment, null);
            }
        });

        Button buttonLogin = view.findViewById(R.id.loginButton);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonPressed = true;
                Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_loginFragment, null);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (buttonPressed) {
            buttonPressed = false;
            //Wait
            new CountDownTimer(400, 1000) {
                public void onTick(long millisecondsUntilDone) {
                    //countdown every second
                }

                public void onFinish() {
                    //Counter is finished
                    Log.i("Done ", "CountDown Finished");
                    AccessibilityManager manager = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
                    if (manager != null && manager.isEnabled()) {
                        AccessibilityEvent e = AccessibilityEvent.obtain();
                        e.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                        e.setClassName(getClass().getName());
                        e.setPackageName(getContext().getPackageName());
                        e.getText().add(getString(R.string.welcome_msg));
                        manager.sendAccessibilityEvent(e);
                    }
                }
            }.start();
        }
    }
}
