package com.example.fridgewise.ui.bottomsheet;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.fridgewise.R;
import com.example.fridgewise.ui.viewmodel.ProfileViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class EditProfileBottomSheet extends BottomSheetDialogFragment {

    private ProfileViewModel viewModel;
    private EditText etUsername, etDOB, etEmail, etPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        etUsername = view.findViewById(R.id.etEditUsername);
        etDOB = view.findViewById(R.id.etEditDOB);
        etEmail = view.findViewById(R.id.etEditEmail);
        etPassword = view.findViewById(R.id.etEditPassword);
        MaterialButton btnDone = view.findViewById(R.id.btnSaveProfile);

        // Pre-fill
        etUsername.setText(viewModel.getUserName().getValue());
        etDOB.setText(viewModel.getUserDOB().getValue());
        etEmail.setText(viewModel.getUserEmail().getValue());
        etPassword.setText(viewModel.getUserPassword().getValue());

        btnDone.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String dob = etDOB.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateFields(username, dob, email, password)) {
                viewModel.updateProfile(username, email, dob);
                viewModel.updateCredentials(email, password);
                Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

    private boolean validateFields(String username, String dob, String email, String password) {
        if (username.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (dob.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your date of birth", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.isEmpty() || password.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
