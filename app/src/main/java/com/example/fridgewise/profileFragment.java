package com.example.fridgewise;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * A fragment that displays the user's profile and settings.
 */
public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private TextView tvUsername;
    private TextView tvUserAge;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvUsername = view.findViewById(R.id.pfp_username_show);
        tvUserAge = view.findViewById(R.id.pfp_user_email_show); // Reusing this for age
        View btnLogout = view.findViewById(R.id.pfp_logout_txt);
        View btnEditProfile = view.findViewById(R.id.btnEditProfile);

        // Observe ViewModel
        viewModel.getUserName().observe(getViewLifecycleOwner(), name -> tvUsername.setText(name));
        viewModel.getUserAge().observe(getViewLifecycleOwner(), age -> 
            tvUserAge.setText(age > 0 ? age + " years old" : "FridgeWise User"));

        // Handle clicks
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        view.findViewById(R.id.btnNotifications).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Notification Settings coming soon", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnHelp).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Help & Support coming soon", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnAbout).setOnClickListener(v -> 
            Toast.makeText(getContext(), "FridgeWise v1.0", Toast.LENGTH_SHORT).show());
    }

    private void showEditProfileDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Edit Profile");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        final EditText etName = new EditText(requireContext());
        etName.setHint("Name");
        etName.setText(viewModel.getUserName().getValue());
        layout.addView(etName);

        final EditText etAge = new EditText(requireContext());
        etAge.setHint("Age");
        etAge.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etAge.setText(String.valueOf(viewModel.getUserAge().getValue()));
        layout.addView(etAge);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();
            
            if (!newName.isEmpty() && !ageStr.isEmpty()) {
                viewModel.updateProfile(newName, Integer.parseInt(ageStr));
                Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out? All your local data will be reset.")
                .setPositiveButton("Logout", (dialog, which) -> {
                    viewModel.logout();
                    Intent intent = new Intent(getActivity(), MainActivity2.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
