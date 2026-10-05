package com.example.fridgewise.ui.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.example.fridgewise.R;
import com.example.fridgewise.data.PreferenceManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.util.Locale;

public class StreakPreferencesBottomSheet extends BottomSheetDialogFragment {

    private PreferenceManager prefManager;
    private OnPreferencesChangedListener listener;

    public interface OnPreferencesChangedListener {
        void onPreferencesChanged();
    }

    public void setOnPreferencesChangedListener(OnPreferencesChangedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_streak_preferences, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefManager = new PreferenceManager(requireContext());

        TextView tvTargetValue = view.findViewById(R.id.tvTargetValue);
        Slider sliderDailyTarget = view.findViewById(R.id.sliderDailyTarget);
        SwitchCompat switchPrefPinNotif = view.findViewById(R.id.switchPrefPinNotif);
        SwitchCompat switchPrefBubble = view.findViewById(R.id.switchPrefBubble);
        LinearLayout btnResetStreak = view.findViewById(R.id.btnResetStreak);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveStreakPrefs);

        // Load existing preferences
        int currentTarget = prefManager.getDailyTaskTarget();
        sliderDailyTarget.setValue(currentTarget);
        tvTargetValue.setText(String.format(Locale.getDefault(), "%d tasks / day", currentTarget));

        switchPrefPinNotif.setChecked(prefManager.isPinNotificationEnabled());
        switchPrefBubble.setChecked(prefManager.isFloatingBubbleEnabled());

        sliderDailyTarget.addOnChangeListener((slider, value, fromUser) -> {
            int val = (int) value;
            tvTargetValue.setText(String.format(Locale.getDefault(), "%d tasks / day", val));
        });

        if (btnResetStreak != null) {
            btnResetStreak.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Reset Streak?")
                        .setMessage("Are you sure you want to reset your current task completion streak count to 0?")
                        .setPositiveButton("Reset", (dialog, which) -> {
                            prefManager.setStreakCount(0);
                            Toast.makeText(requireContext(), "Streak reset to 0 days", Toast.LENGTH_SHORT).show();
                            if (listener != null) listener.onPreferencesChanged();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                int newTarget = (int) sliderDailyTarget.getValue();
                prefManager.setDailyTaskTarget(newTarget);
                prefManager.setPinNotificationEnabled(switchPrefPinNotif.isChecked());
                prefManager.setFloatingBubbleEnabled(switchPrefBubble.isChecked());

                Toast.makeText(requireContext(), "Streak preferences saved", Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onPreferencesChanged();
                dismiss();
            });
        }
    }
}
