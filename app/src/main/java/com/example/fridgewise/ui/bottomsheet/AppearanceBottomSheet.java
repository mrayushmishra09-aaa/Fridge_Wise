package com.example.fridgewise.ui.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.fridgewise.R;
import com.example.fridgewise.data.PreferenceManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class AppearanceBottomSheet extends BottomSheetDialogFragment {

    private PreferenceManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_appearance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefManager = new PreferenceManager(requireContext());

        RadioGroup rgTheme = view.findViewById(R.id.rgTheme);
        RadioButton rbSystem = view.findViewById(R.id.rbSystem);
        RadioButton rbLight = view.findViewById(R.id.rbLight);
        RadioButton rbDark = view.findViewById(R.id.rbDark);
        MaterialButton btnApply = view.findViewById(R.id.btnApplyTheme);

        int currentTheme = prefManager.getAppTheme();
        if (currentTheme == 1) rbLight.setChecked(true);
        else if (currentTheme == 2) rbDark.setChecked(true);
        else rbSystem.setChecked(true);

        btnApply.setOnClickListener(v -> {
            int selectedTheme = 0;
            if (rbLight.isChecked()) selectedTheme = 1;
            else if (rbDark.isChecked()) selectedTheme = 2;

            prefManager.setAppTheme(selectedTheme);
            applyTheme(selectedTheme);
            dismiss();
        });
    }

    private void applyTheme(int theme) {
        switch (theme) {
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
