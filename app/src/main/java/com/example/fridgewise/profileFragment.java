package com.example.fridgewise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A fragment that displays the user's profile and settings.
 */
public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        TextView tvUsername = view.findViewById(R.id.pfp_username_show);
        TextView tvEmail = view.findViewById(R.id.pfp_user_email_show);
        View btnLogout = view.findViewById(R.id.pfp_logout_txt);
        View btnEditProfile = view.findViewById(R.id.btnEditProfile);
        View btnNotifications = view.findViewById(R.id.btnNotifications);
        View btnHelp = view.findViewById(R.id.btnHelp);
        View btnAbout = view.findViewById(R.id.btnAbout);

        // Set dummy data (Replace with real user data later)
        tvUsername.setText("Ayush");
        tvEmail.setText("ayush@fridgewise.com");

        // Handle clicks
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            // Implement actual logout logic here
        });

        btnEditProfile.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show()
        );

        btnNotifications.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Notification Settings clicked", Toast.LENGTH_SHORT).show()
        );

        btnHelp.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Help & Support clicked", Toast.LENGTH_SHORT).show()
        );

        btnAbout.setOnClickListener(v -> 
            Toast.makeText(getContext(), "About FridgeWise clicked", Toast.LENGTH_SHORT).show()
        );
    }
}
