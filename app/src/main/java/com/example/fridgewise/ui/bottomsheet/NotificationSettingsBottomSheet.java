package com.example.fridgewise.ui.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.example.fridgewise.R;
import com.example.fridgewise.data.PreferenceManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class NotificationSettingsBottomSheet extends BottomSheetDialogFragment {

    private PreferenceManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_notification_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefManager = new PreferenceManager(requireContext());

        SwitchCompat swExpiry = view.findViewById(R.id.switchExpiryNotif);
        SwitchCompat swGrocery = view.findViewById(R.id.switchGroceryNotif);
        SwitchCompat swInsights = view.findViewById(R.id.switchInsightsNotif);
        MaterialButton btnDone = view.findViewById(R.id.btnDoneNotif);

        // Set initial states
        swExpiry.setChecked(prefManager.isNotifExpiryEnabled());
        swGrocery.setChecked(prefManager.isNotifGroceryEnabled());
        swInsights.setChecked(prefManager.isSmartFollowUpEnabled()); // Linking Smart insights to Smart follow up for now

        btnDone.setOnClickListener(v -> {
            prefManager.setNotifExpiryEnabled(swExpiry.isChecked());
            prefManager.setNotifGroceryEnabled(swGrocery.isChecked());
            prefManager.setSmartFollowUpEnabled(swInsights.isChecked());
            dismiss();
        });
    }
}
