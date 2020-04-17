package fau.amoracen.guiderobot.ui.mainactivity;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import fau.amoracen.guiderobot.R;
import fau.amoracen.guiderobot.service.MyAccessibilityEvent;

/**
 * HomeFragment displays the button to Create Account and Login
 */
public class HomeFragment extends Fragment {

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
                Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_regitrationFragment, null);
            }
        });

        Button buttonLogin = view.findViewById(R.id.loginButton);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_loginFragment, null);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        /*Start an Accessibility Event*/
        MyAccessibilityEvent.getInstance().sendAccessibilityEvent(getContext(), getString(R.string.welcome_msg));
    }
}
