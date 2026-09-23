package com.example.fridgewise.ui.fragments;

import com.example.fridgewise.R;
import com.example.fridgewise.data.*;
import com.example.fridgewise.model.*;
import com.example.fridgewise.adapter.*;
import com.example.fridgewise.ui.bottomsheet.MemoryPreferencesBottomSheet;
import com.example.fridgewise.util.*;
import com.example.fridgewise.ui.viewmodel.*;
import com.example.fridgewise.ui.activities.*;
import com.example.fridgewise.ui.bottomsheet.EditProfileBottomSheet;
import com.example.fridgewise.ui.bottomsheet.NotificationSettingsBottomSheet;
import com.example.fridgewise.ui.bottomsheet.AppearanceBottomSheet;

import com.example.fridgewise.R;

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
    private TextView tvUserEmail;
    private TextView tvUserDOB;
    private ImageView ivUserProfile;
    private View profileCameraIcon;
    private View btnEditProfile;
    private View btnDeleteAccount;

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
        tvUserEmail = view.findViewById(R.id.pfp_user_email_show);
        
        TextView tvUserIdDisplay = new TextView(getContext());
        // We will bind text inside the observer
        
        // Let's bind User ID safely inside the view hierarchy or update the fields cleanly
        // tvUserDOB = view.findViewById(R.id.pfp_user_dob_show);
        ivUserProfile = view.findViewById(R.id.ivUserProfile);
        profileCameraIcon = view.findViewById(R.id.pfp_img_add);
        
        View btnLogout = view.findViewById(R.id.pfp_logout_txt);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        // btnDeleteAccount = view.findViewById(R.id.btn_delete_account);
        View cardProfileImage = view.findViewById(R.id.cardProfileImage);

        // Observe ViewModel
        viewModel.getUserName().observe(getViewLifecycleOwner(), name -> tvUsername.setText(name));
        
        viewModel.getUserId().observe(getViewLifecycleOwner(), uid -> {
            if (uid != null && !uid.isEmpty()) {
                tvUsername.setText(uid);
            }
        });

        viewModel.getUserEmail().observe(getViewLifecycleOwner(), email -> tvUserEmail.setText(email));
        
        viewModel.getAuthProvider().observe(getViewLifecycleOwner(), provider -> {
            View badge = getView() != null ? getView().findViewById(R.id.badge_member) : null;
            if (badge instanceof LinearLayout && getView() != null) {
                TextView badgeText = (TextView) ((LinearLayout) badge).getChildAt(1);
                if ("GUEST".equals(provider)) {
                    badgeText.setText(R.string.guest_user);
                    tvUserEmail.setText(R.string.guest_mode_desc);
                } else if ("GOOGLE".equals(provider)) {
                    badgeText.setText("Google Account");
                    if (viewModel.getUserEmail().getValue() != null) {
                        tvUserEmail.setText(viewModel.getUserEmail().getValue());
                    }
                }
            }
        });
        /*
        viewModel.getUserDOB().observe(getViewLifecycleOwner(), dob -> {
            if (tvUserDOB != null) tvUserDOB.setText("Date of Birth: " + dob);
        });
        */
        
        viewModel.getLogoutCompleted().observe(getViewLifecycleOwner(), completed -> {
            if (completed) {
                navigateToMain();
            }
        });
        
        viewModel.getProfileImageUri().observe(getViewLifecycleOwner(), uriString -> {
            if (uriString != null && !uriString.isEmpty()) {
                try {
                    Uri uri = Uri.parse(uriString);
                    ivUserProfile.setImageURI(uri);
                    ivUserProfile.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    ivUserProfile.setImageResource(R.drawable.ic_default_avatar);
                    ivUserProfile.setVisibility(View.VISIBLE);
                }
            } else {
                ivUserProfile.setImageResource(R.drawable.ic_default_avatar);
                ivUserProfile.setVisibility(View.VISIBLE);
            }
            // Camera icon stays visible as a professional "Edit" trigger
            profileCameraIcon.setVisibility(View.VISIBLE);
        });

        // Handle clicks
        cardProfileImage.setOnClickListener(v -> 
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        btnEditProfile.setOnClickListener(v -> showEditProfileBottomSheet());
        view.findViewById(R.id.row_personal_details).setOnClickListener(v -> showEditProfileBottomSheet());
        
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        
        /*
        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());
        }
        */

        view.findViewById(R.id.btnNotifications).setOnClickListener(v -> 
            new NotificationSettingsBottomSheet().show(getParentFragmentManager(), "notif_settings"));

        view.findViewById(R.id.btn_appearance).setOnClickListener(v -> 
            new AppearanceBottomSheet().show(getParentFragmentManager(), "appearance_settings"));

        /*
        View btnMemoryPref = view.findViewById(R.id.btn_memory_pref);
        if (btnMemoryPref != null) {
            btnMemoryPref.setOnClickListener(v -> 
                new MemoryPreferencesBottomSheet().show(getParentFragmentManager(), "memory_preferences"));
        }
        */

        view.findViewById(R.id.btnHelp).setOnClickListener(v -> openHelpEmail());

        /*
        View btnAbout = view.findViewById(R.id.btnAbout);
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> showAboutDialog());
        }
        */
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("About FridgeWise")
                .setMessage("FridgeWise v1.2.0\n\nYour professional kitchen companion. Smartly tracking food, medicine, and shopping tasks.\n\n© 2024 FridgeWise Team")
                .setPositiveButton("Close", null)
                .setIcon(R.drawable.ic_info)
                .show();
    }

    private void openHelpEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:support@fridgewise.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "FridgeWise Feedback - " + tvUsername.getText());
        try {
            startActivity(Intent.createChooser(intent, "Send Feedback via..."));
        } catch (Exception e) {
            Toast.makeText(getContext(), "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditProfileBottomSheet() {
        new EditProfileBottomSheet().show(getParentFragmentManager(), "edit_profile");
    }

    private void showDeleteAccountConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account?")
                .setMessage("This action is permanent and cannot be undone. All your fridge data and memories will be lost forever.")
                .setPositiveButton("Delete Forever", (dialog, which) -> {
                    Toast.makeText(getContext(), "Account Deleted Successfully", Toast.LENGTH_SHORT).show();
                    viewModel.logout();
                })
                .setNegativeButton("Keep My Account", null)
                .setIcon(R.drawable.outline_delete_24)
                .show();
    }

    private void navigateToMain() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    viewModel.logout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleProfileImagePicked(Uri uri) {
        try {
            // Save image to internal storage to ensure it persists even if deleted from gallery
            String internalPath = FileUtil.saveProfileImage(requireContext(), uri);
            
            if (internalPath != null) {
                viewModel.updateProfileImage(internalPath);
                Toast.makeText(getContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show();
            } else {
                // Fallback to original URI if saving fails
                viewModel.updateProfileImage(uri.toString());
                Toast.makeText(getContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            viewModel.updateProfileImage(uri.toString());
            Toast.makeText(getContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show();
        }
    }
}
