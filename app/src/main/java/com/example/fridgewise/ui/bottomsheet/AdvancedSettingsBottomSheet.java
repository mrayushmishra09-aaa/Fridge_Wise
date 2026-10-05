package com.example.fridgewise.ui.bottomsheet;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.example.fridgewise.R;
import com.example.fridgewise.data.PreferenceManager;
import com.example.fridgewise.util.BackupManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.io.File;

public class AdvancedSettingsBottomSheet extends BottomSheetDialogFragment {

    private PreferenceManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_advanced_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefManager = new PreferenceManager(requireContext());

        SwitchCompat switchBiometric = view.findViewById(R.id.switchBiometricLock);
        SwitchCompat switchHaptic = view.findViewById(R.id.switchHaptic);
        MaterialButton btnExport = view.findViewById(R.id.btnExportBackup);
        MaterialButton btnRestore = view.findViewById(R.id.btnRestoreBackup);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveAdvanced);

        switchBiometric.setChecked(prefManager.isBiometricLockEnabled());
        switchHaptic.setChecked(prefManager.isHapticFeedbackEnabled());

        btnExport.setOnClickListener(v -> {
            File backupFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "fridgewise_backup.json");
            boolean success = BackupManager.exportToFile(requireContext(), backupFile);
            if (success) {
                Toast.makeText(requireContext(), "Backup exported to: " + backupFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), "Failed to export backup", Toast.LENGTH_SHORT).show();
            }
        });

        btnRestore.setOnClickListener(v -> {
            File backupFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "fridgewise_backup.json");
            if (!backupFile.exists()) {
                Toast.makeText(requireContext(), "No backup file found in Documents", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean success = BackupManager.restoreFromFile(requireContext(), backupFile);
            if (success) {
                Toast.makeText(requireContext(), "Backup restored successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Failed to restore backup", Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(v -> {
            prefManager.setBiometricLockEnabled(switchBiometric.isChecked());
            prefManager.setHapticFeedbackEnabled(switchHaptic.isChecked());
            Toast.makeText(requireContext(), "Advanced preferences saved", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}
