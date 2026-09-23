package com.example.fridgewise.ui.fragments;

import com.example.fridgewise.R;
import com.example.fridgewise.data.*;
import com.example.fridgewise.model.*;
import com.example.fridgewise.ui.viewmodel.*;
import com.example.fridgewise.ui.activities.*;
import com.example.fridgewise.ui.bottomsheet.*;

import com.example.fridgewise.util.AuthManager;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class RegistrationFragment extends Fragment {

    private EditText etName, etAge;
    private OnboardingViewModel viewModel;
    private AuthManager authManager;

    public RegistrationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        etName = view.findViewById(R.id.etName);
        etAge = view.findViewById(R.id.etAge);
        
        etAge.setOnClickListener(v -> showDatePicker());
        
        authManager = new AuthManager(requireContext());

        view.findViewById(R.id.btnGoogleSignIn).setOnClickListener(v -> handleGoogleSignIn());
        view.findViewById(R.id.btnContinueGuest).setOnClickListener(v -> handleContinueAsGuest());

        return view;
    }

    private void handleGoogleSignIn() {
        authManager.signInWithGoogle(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String name, String email, String profilePictureUri) {
                PreferenceManager prefManager = new PreferenceManager(requireContext());
                
                // Check if profile exists for this email/google account already
                String existingUserId = prefManager.getUserId();
                if (existingUserId != null && prefManager.getUserEmail().equals(email)) {
                    // Returning Google user who already has a complete profile linked
                    prefManager.setLoggedIn(true);
                    prefManager.setGuest(false);
                    prefManager.setAuthProvider("GOOGLE");
                    
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Welcome back, " + existingUserId, Toast.LENGTH_SHORT).show();
                        viewModel.onRegistrationProcessed(true);
                    });
                    return;
                }

                // New profile creation required or updating info
                prefManager.setUserEmail(email);
                if (profilePictureUri != null) {
                    prefManager.setProfileImageUri(profilePictureUri);
                }
                
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Authenticated securely via Google! Please choose your unique User ID below to complete setup.", Toast.LENGTH_LONG).show();
                    etName.requestFocus();
                });
            }

            @Override
            public void onError(String message) {
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "Google authentication error: " + message, Toast.LENGTH_LONG).show());
            }

            @Override
            public void onCancel() {
                // Do nothing
            }
        });
    }

    private void handleContinueAsGuest() {
        PreferenceManager prefManager = new PreferenceManager(requireContext());
        int randomNum = (int) (Math.random() * 90000) + 10000;
        String guestId = "Guest_" + randomNum;
        
        prefManager.setUserId(guestId);
        prefManager.setUserName("Guest Assistant");
        prefManager.setLoggedIn(false);
        prefManager.setGuest(true);
        prefManager.setAuthProvider("GUEST");
        
        Toast.makeText(getContext(), "Entering as temporary guest: " + guestId, Toast.LENGTH_SHORT).show();
        viewModel.onRegistrationProcessed(true);
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date of Birth")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etAge.setText(format.format(calendar.getTime()));
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);
        
        viewModel.getShouldSaveRegistration().observe(getViewLifecycleOwner(), shouldSave -> {
            if (shouldSave) {
                if (validateAndSave()) {
                    viewModel.onRegistrationProcessed(true);
                } else {
                    viewModel.onRegistrationProcessed(false);
                }
            }
        });
    }

    public boolean validateAndSave() {
        String userId = etName.getText().toString().trim();
        String dob = etAge.getText().toString().trim();

        if (TextUtils.isEmpty(userId)) {
            etName.setError("User ID is required");
            etName.requestFocus();
            return false;
        }
        
        if (userId.length() < 3) {
            etName.setError("User ID must be at least 3 characters");
            etName.requestFocus();
            return false;
        }

        PreferenceManager prefManager = new PreferenceManager(requireContext());
        
        // Finalize state
        prefManager.setUserId(userId);
        if (prefManager.getUserName().equals("User")) {
             // Default was "User", we can set it to the ID or keep generic
             prefManager.setUserName(userId);
        }

        if (!dob.isEmpty()) {
            prefManager.setUserDOB(dob);
        }
        
        // If they reached here, they either came from Google (email set) or manual
        if (prefManager.getUserEmail() != null && !prefManager.getUserEmail().equals("user@email.com")) {
            prefManager.setLoggedIn(true);
            prefManager.setGuest(false);
            prefManager.setAuthProvider("GOOGLE");
        } else {
            // Manual local setup
            prefManager.setLoggedIn(false);
            prefManager.setGuest(false);
        }
        
        return true;
    }
}
