package fau.amoracen.guiderobot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

/**
 * Height Section
 */
public class RegistrationHeightFragment extends Fragment {
    private TextInputLayout heightFeetLayout;
    private TextInputLayout heightInchesLayout;
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
        /*Height in INCHES*/
        heightInchesLayout = view.findViewById(R.id.inchesTextInputLayout);

        Button buttonNextPage = view.findViewById(R.id.nextPageButton);
        buttonNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateHeight()) {
                    bundle.putString("heightFeet", feetInput);
                    bundle.putString("heightInches", inchesInput);
                    //Navigation.findNavController(view).navigate(R.id.action_registrationHeightFragment_to_PasswordFragment, bundle);
                }
                /*TODO*/
                //bundle.putString("heightFeet", "5");
                //bundle.putString("heightInches", "10");
                //Navigation.findNavController(view).navigate(R.id.action_registrationHeightFragment_to_PasswordFragment, bundle);
            }
        });
    }

    /**
     * Check Height.
     *
     * @return true if height is valid
     */
    private boolean validateHeight() {
        /*Checking Height Feet*/
        feetInput = Objects.requireNonNull(heightFeetLayout.getEditText()).getText().toString().trim();
        heightFeetLayout.setErrorEnabled(true);
        if (feetInput.isEmpty()) {
            heightFeetLayout.setError("Field for feet can't be empty");
            return false;
        }
        int feet = Integer.parseInt(feetInput);
        if (feet < 3 || feet > 8) {
            heightFeetLayout.setError("Please check the height in feet");
            return false;
        } else {
            heightFeetLayout.setErrorEnabled(false);
        }
        /*Checking Height Inches*/
        inchesInput = Objects.requireNonNull(heightInchesLayout.getEditText()).getText().toString().trim();
        heightInchesLayout.setErrorEnabled(true);
        if (inchesInput.isEmpty()) {
            heightInchesLayout.setError("Field for inches can't be empty");
            return false;
        }
        int inches = Integer.parseInt(inchesInput);
        if (inches > 11) {
            heightInchesLayout.setError("Please check the height in inches");
            return false;
        } else {
            heightInchesLayout.setErrorEnabled(false);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        /*Start an Accessibility Event*/
        MyAccessibilityEvent.getInstance().sendAccessibilityEvent(getContext(), "Registration Form.Height Screen");
    }
}
