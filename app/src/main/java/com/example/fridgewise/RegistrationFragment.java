package com.example.fridgewise;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RegistrationFragment extends Fragment {

    public RegistrationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        EditText etName = view.findViewById(R.id.etName);
        EditText etAge = view.findViewById(R.id.etAge);
        View btnSubmit = view.findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("Name is required");
                return;
            }

            if (TextUtils.isEmpty(ageStr)) {
                etAge.setError("Age is required");
                return;
            }

            int age = Integer.parseInt(ageStr);
            if (age <= 0 || age > 120) {
                etAge.setError("Please enter a valid age");
                return;
            }

            PreferenceManager prefManager = new PreferenceManager(requireContext());
            prefManager.setUserName(name);
            prefManager.setUserAge(age);

            if (getActivity() instanceof OnboardingActivity) {
                ((OnboardingActivity) getActivity()).finishOnboarding();
            }
        });

        return view;
    }
}
