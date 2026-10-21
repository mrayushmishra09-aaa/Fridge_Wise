package com.example.fridgewise.ui.bottomsheet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.example.fridgewise.R;
import com.example.fridgewise.data.AppDatabase;
import com.example.fridgewise.data.PreferenceManager;
import com.example.fridgewise.model.CustomSpace;
import com.example.fridgewise.ui.dialog.SecurityAuthDialog;
import com.example.fridgewise.ui.dialog.SetupPinDialog;
import com.example.fridgewise.util.BackupManager;
import com.example.fridgewise.util.SecurityManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AdvancedSettingsBottomSheet extends BottomSheetDialogFragment {

    private PreferenceManager prefManager;
    private SecurityManager secManager;

    private final ActivityResultLauncher<Intent> exportLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    android.net.Uri uri = result.getData().getData();
                    if (uri != null) {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            boolean success = BackupManager.exportToUri(requireContext(), uri);
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> {
                                    if (success) {
                                        Toast.makeText(requireContext(), "Backup exported successfully!", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(requireContext(), "Failed to export backup", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> restoreLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    android.net.Uri uri = result.getData().getData();
                    if (uri != null) {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            boolean success = BackupManager.restoreFromUri(requireContext(), uri);
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> {
                                    if (success) {
                                        Toast.makeText(requireContext(), "Backup restored successfully!", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(requireContext(), "Failed to restore backup", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_advanced_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefManager = new PreferenceManager(requireContext());
        secManager = SecurityManager.getInstance(requireContext());

        SwitchCompat switchMasterLock = view.findViewById(R.id.switchMasterLock);
        MaterialButton btnChangeMasterPin = view.findViewById(R.id.btnChangeMasterPin);
        MaterialButton btnViewRecoveryKey = view.findViewById(R.id.btnViewRecoveryKey);
        SwitchCompat switchBiometric = view.findViewById(R.id.switchBiometricLock);
        MaterialButton btnManageHiddenSpaces = view.findViewById(R.id.btnManageHiddenSpaces);
        SwitchCompat switchHaptic = view.findViewById(R.id.switchHaptic);

        MaterialButton btnExport = view.findViewById(R.id.btnExportBackup);
        MaterialButton btnRestore = view.findViewById(R.id.btnRestoreBackup);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveAdvanced);

        boolean masterLockEnabled = secManager.isMasterLockEnabled();
        switchMasterLock.setChecked(masterLockEnabled);
        btnChangeMasterPin.setVisibility(masterLockEnabled ? View.VISIBLE : View.GONE);
        btnViewRecoveryKey.setVisibility(masterLockEnabled ? View.VISIBLE : View.GONE);

        switchBiometric.setChecked(secManager.isBiometricEnabled());
        switchHaptic.setChecked(prefManager.isHapticFeedbackEnabled());

        switchMasterLock.setOnClickListener(v -> {
            boolean isChecked = switchMasterLock.isChecked();
            if (isChecked) {
                // Uncheck temporarily until setup completes
                switchMasterLock.setChecked(false);
                SetupPinDialog dialog = SetupPinDialog.newInstance("Set Master PIN", new SetupPinDialog.SetupPinCallback() {
                    @Override
                    public void onPinCreated(String pin, String salt, String hash) {
                        boolean success = secManager.setupMasterPin(pin);
                        if (success) {
                            switchMasterLock.setChecked(true);
                            btnChangeMasterPin.setVisibility(View.VISIBLE);
                            btnViewRecoveryKey.setVisibility(View.VISIBLE);
                            Toast.makeText(requireContext(), "Master PIN enabled", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to set Master PIN", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCanceled() {
                        switchMasterLock.setChecked(false);
                    }
                });
                dialog.show(getParentFragmentManager(), "setup_pin_dialog");
            } else {
                // Re-check temporarily until current PIN is authenticated
                switchMasterLock.setChecked(true);
                SecurityAuthDialog authDialog = SecurityAuthDialog.newInstance(new SecurityAuthDialog.AuthCallback() {
                    @Override
                    public void onAuthenticated() {
                        secManager.clearMasterPin();
                        switchBiometric.setChecked(false);
                        switchMasterLock.setChecked(false);
                        btnChangeMasterPin.setVisibility(View.GONE);
                        btnViewRecoveryKey.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Master PIN disabled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCanceled() {
                        switchMasterLock.setChecked(true);
                    }
                });
                authDialog.show(getParentFragmentManager(), "auth_dialog");
            }
        });

        btnChangeMasterPin.setOnClickListener(v -> {
            SecurityAuthDialog authDialog = SecurityAuthDialog.newInstance(new SecurityAuthDialog.AuthCallback() {
                @Override
                public void onAuthenticated() {
                    SetupPinDialog setupDialog = SetupPinDialog.newInstance("Change Master PIN", new SetupPinDialog.SetupPinCallback() {
                        @Override
                        public void onPinCreated(String pin, String salt, String hash) {
                            secManager.setupMasterPin(pin);
                            Toast.makeText(requireContext(), "Master PIN updated successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCanceled() {}
                    });
                    setupDialog.show(getParentFragmentManager(), "change_pin_dialog");
                }

                @Override
                public void onCanceled() {}
            });
            authDialog.show(getParentFragmentManager(), "auth_dialog");
        });

        btnViewRecoveryKey.setOnClickListener(v -> {
            SecurityAuthDialog authDialog = SecurityAuthDialog.newInstance(new SecurityAuthDialog.AuthCallback() {
                @Override
                public void onAuthenticated() {
                    String key = secManager.generateAndStoreRecoveryKey();
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Emergency Recovery Key 🔑")
                            .setMessage("Your Emergency Recovery Key:\n\n" + key + "\n\nStore this key safely. If you ever forget your PIN, this is required to reset it.")
                            .setPositiveButton("Copy to Clipboard", (d, w) -> {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("Recovery Key", key);
                                if (clipboard != null) clipboard.setPrimaryClip(clip);
                                Toast.makeText(requireContext(), "Recovery Key copied!", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Close", null)
                            .show();
                }

                @Override
                public void onCanceled() {}
            });
            authDialog.show(getParentFragmentManager(), "auth_view_recovery");
        });

        switchBiometric.setOnClickListener(v -> {
            boolean isChecked = switchBiometric.isChecked();
            if (isChecked) {
                if (!secManager.isMasterLockEnabled()) {
                    switchBiometric.setChecked(false);
                    Toast.makeText(requireContext(), "Set up Master PIN first before enabling Biometrics", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!secManager.isBiometricAvailable(requireContext())) {
                    switchBiometric.setChecked(false);
                    Toast.makeText(requireContext(), "Biometric hardware not available or fingerprint not registered", Toast.LENGTH_SHORT).show();
                    return;
                }
                secManager.setBiometricEnabled(true);
                Toast.makeText(requireContext(), "Biometric unlock enabled", Toast.LENGTH_SHORT).show();
            } else {
                secManager.setBiometricEnabled(false);
            }
        });

        btnManageHiddenSpaces.setOnClickListener(v -> {
            Runnable showHiddenDialog = () -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    AppDatabase db = AppDatabase.getInstance(requireContext());
                    List<CustomSpace> allSpaces = db.customSpaceDao().getAllSpacesSync();
                    List<CustomSpace> hiddenSpaces = new ArrayList<>();
                    for (CustomSpace s : allSpaces) {
                        if (s.isHidden()) hiddenSpaces.add(s);
                    }

                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            if (hiddenSpaces.isEmpty()) {
                                Toast.makeText(requireContext(), "No hidden spaces found", Toast.LENGTH_SHORT).show();
                            } else {
                                String[] names = new String[hiddenSpaces.size()];
                                for (int i = 0; i < hiddenSpaces.size(); i++) {
                                    names[i] = "📁 " + hiddenSpaces.get(i).getName();
                                }

                                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                        .setTitle("Manage Hidden Spaces")
                                        .setItems(names, (dialog, which) -> {
                                            CustomSpace selected = hiddenSpaces.get(which);
                                            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                                    .setTitle(selected.getName())
                                                    .setMessage("Would you like to unhide this space?")
                                                    .setPositiveButton("Unhide 👁", (d, w) -> {
                                                        selected.setHidden(false);
                                                        Executors.newSingleThreadExecutor().execute(() -> {
                                                            db.customSpaceDao().updateSpace(selected);
                                                            requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Space unhidden!", Toast.LENGTH_SHORT).show());
                                                        });
                                                    })
                                                    .setNegativeButton("Cancel", null)
                                                    .show();
                                        })
                                        .setNegativeButton("Close", null)
                                        .show();
                            }
                        });
                    }
                });
            };

            if (secManager.isMasterLockEnabled()) {
                SecurityAuthDialog authDialog = SecurityAuthDialog.newInstance(new SecurityAuthDialog.AuthCallback() {
                    @Override
                    public void onAuthenticated() {
                        showHiddenDialog.run();
                    }

                    @Override
                    public void onCanceled() {}
                });
                authDialog.show(getParentFragmentManager(), "auth_hidden_spaces");
            } else {
                showHiddenDialog.run();
            }
        });

        btnExport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "fridgewise_backup_" + System.currentTimeMillis() + ".json");
            exportLauncher.launch(intent);
        });

        btnRestore.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            restoreLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {
            prefManager.setHapticFeedbackEnabled(switchHaptic.isChecked());
            Toast.makeText(requireContext(), "Advanced preferences saved", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}
