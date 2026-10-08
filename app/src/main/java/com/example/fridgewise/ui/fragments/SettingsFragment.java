package com.example.fridgewise.ui.fragments;

import com.example.fridgewise.R;
import com.example.fridgewise.ui.bottomsheet.NotificationSettingsBottomSheet;
import com.example.fridgewise.ui.bottomsheet.AppearanceBottomSheet;
import com.example.fridgewise.ui.bottomsheet.AdvancedSettingsBottomSheet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * A fragment that displays app settings and preferences.
 */
public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Update header texts if needed or handle settings clicks
        View btnNotifications = view.findViewById(R.id.btnNotifications);
        View btnAppearance = view.findViewById(R.id.btn_appearance);
        View btnAdvanced = view.findViewById(R.id.btn_advanced_settings);
        View btnAiConnector = view.findViewById(R.id.btn_ai_connector);
        View btnHelp = view.findViewById(R.id.btnHelp);
        View rowPersonalDetails = view.findViewById(R.id.row_personal_details);
        View btnLogout = view.findViewById(R.id.pfp_logout_txt);

        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> 
                new NotificationSettingsBottomSheet().show(getParentFragmentManager(), "notif_settings"));
        }

        if (btnAppearance != null) {
            btnAppearance.setOnClickListener(v -> 
                new AppearanceBottomSheet().show(getParentFragmentManager(), "appearance_settings"));
        }

        if (btnAdvanced != null) {
            btnAdvanced.setOnClickListener(v -> 
                new AdvancedSettingsBottomSheet().show(getParentFragmentManager(), "advanced_settings"));
        }

        if (btnAiConnector != null) {
            btnAiConnector.setOnClickListener(v -> 
                Navigation.findNavController(v).navigate(R.id.nav_ai_connector));
        }

        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> openHelpEmail());
        }

        if (rowPersonalDetails != null) {
            rowPersonalDetails.setOnClickListener(v -> showAboutDialog());
        }

        if (btnLogout != null) {
            btnLogout.setVisibility(View.GONE); // No logout needed without login system
        }
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
        intent.putExtra(Intent.EXTRA_SUBJECT, "FridgeWise Feedback");
        try {
            startActivity(Intent.createChooser(intent, "Send Feedback via..."));
        } catch (Exception e) {
            Toast.makeText(getContext(), "No email app found", Toast.LENGTH_SHORT).show();
        }
    }
}
