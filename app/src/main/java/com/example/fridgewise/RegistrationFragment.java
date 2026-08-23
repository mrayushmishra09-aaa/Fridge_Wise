package com.example.fridgewise;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RegistrationFragment extends Fragment {

    private EditText etName, etAge;

    public RegistrationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        etName = view.findViewById(R.id.etName);
        etAge = view.findViewById(R.id.etAge);

        return view;
    }

    public boolean validateAndSave() {
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }

        int age = 0;
        if (!TextUtils.isEmpty(ageStr)) {
            try {
                age = Integer.parseInt(ageStr);
                if (age <= 0 || age > 120) {
                    etAge.setError("Please enter a valid age");
                    etAge.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                etAge.setError("Invalid number");
                etAge.requestFocus();
                return false;
            }
        }

        PreferenceManager prefManager = new PreferenceManager(requireContext());
        prefManager.setUserName(name);
        prefManager.setUserAge(age);
        
        return true;
    }
}
