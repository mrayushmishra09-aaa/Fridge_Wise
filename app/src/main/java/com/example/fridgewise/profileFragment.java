package com.example.fridgewise;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
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
    private ImageView ivUserProfile;
    private View profileCameraIcon;
    private TextView tvAddPhotoLabel;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    handleProfileImagePicked(uri);
                }
            });

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
        ivUserProfile = view.findViewById(R.id.ivUserProfile);
        profileCameraIcon = view.findViewById(R.id.pfp_img_add);
        tvAddPhotoLabel = view.findViewById(R.id.pfp_txt01);
        
        View btnLogout = view.findViewById(R.id.pfp_logout_txt);
        View btnEditProfile = view.findViewById(R.id.btnEditProfile);
        View cardProfileImage = view.findViewById(R.id.cardProfileImage);

        // Observe ViewModel
        viewModel.getUserName().observe(getViewLifecycleOwner(), name -> tvUsername.setText(name));
        viewModel.getUserAge().observe(getViewLifecycleOwner(), age -> 
            tvUserAge.setText(age > 0 ? age + " years old" : "FridgeWise User"));
        
        viewModel.getProfileImageUri().observe(getViewLifecycleOwner(), uriString -> {
            if (uriString != null) {
                try {
                    Uri uri = Uri.parse(uriString);
                    ivUserProfile.setImageURI(uri);
                    ivUserProfile.setVisibility(View.VISIBLE);
                    profileCameraIcon.setVisibility(View.GONE);
                    tvAddPhotoLabel.setVisibility(View.GONE);
                } catch (Exception e) {
                    ivUserProfile.setVisibility(View.GONE);
                    profileCameraIcon.setVisibility(View.VISIBLE);
                    tvAddPhotoLabel.setVisibility(View.VISIBLE);
                }
            } else {
                ivUserProfile.setVisibility(View.GONE);
                profileCameraIcon.setVisibility(View.VISIBLE);
                tvAddPhotoLabel.setVisibility(View.VISIBLE);
            }
        });

        // Handle clicks
        cardProfileImage.setOnClickListener(v -> 
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        view.findViewById(R.id.btnNotifications).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Notification Settings coming soon", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnHelp).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Help & Support coming soon", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnAbout).setOnClickListener(v -> 
            Toast.makeText(getContext(), "FridgeWise v1.0", Toast.LENGTH_SHORT).show());

        // Preferences Switches
        SwitchCompat switchFollowUp = view.findViewById(R.id.switchSmartFollowUp);
        SwitchCompat switchExpiry = view.findViewById(R.id.switchAdvanceExpiry);

        PreferenceManager pref = new PreferenceManager(requireContext());
        switchFollowUp.setChecked(pref.isSmartFollowUpEnabled());
        switchExpiry.setChecked(pref.isAdvanceExpiryEnabled());

        switchFollowUp.setOnCheckedChangeListener((buttonView, isChecked) -> 
            pref.setSmartFollowUpEnabled(isChecked));
        
        switchExpiry.setOnCheckedChangeListener((buttonView, isChecked) -> 
            pref.setAdvanceExpiryEnabled(isChecked));
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

    private void handleProfileImagePicked(Uri uri) {
        try {
            // Take persistable permission to keep access after reboot
            requireContext().getContentResolver().takePersistableUriPermission(uri, 
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            viewModel.updateProfileImage(uri.toString());
            Toast.makeText(getContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // If persistable permission fails, just save the URI
            viewModel.updateProfileImage(uri.toString());
            Toast.makeText(getContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show();
        }
    }
}
