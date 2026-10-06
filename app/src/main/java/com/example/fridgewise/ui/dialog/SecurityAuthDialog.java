package com.example.fridgewise.ui.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.fridgewise.R;
import com.example.fridgewise.model.CustomSpace;
import com.example.fridgewise.util.HapticUtils;
import com.example.fridgewise.util.SecurityManager;
import com.google.android.material.button.MaterialButton;

public class SecurityAuthDialog extends DialogFragment {

    public interface AuthCallback {
        void onAuthenticated();
        void onCanceled();
    }

    private CustomSpace targetSpace;
    private AuthCallback callback;
    private final StringBuilder enteredPin = new StringBuilder();

    private final View[] dots = new View[4];
    private TextView tvAuthTitle;
    private View llPinDots;

    public static SecurityAuthDialog newInstance(CustomSpace space, AuthCallback callback) {
        SecurityAuthDialog dialog = new SecurityAuthDialog();
        dialog.targetSpace = space;
        dialog.callback = callback;
        return dialog;
    }

    public static SecurityAuthDialog newInstance(AuthCallback callback) {
        return newInstance(null, callback);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.dialog_security_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvAuthTitle = view.findViewById(R.id.tvAuthTitle);
        TextView tvAuthSubtitle = view.findViewById(R.id.tvAuthSubtitle);
        llPinDots = view.findViewById(R.id.llPinDots);

        dots[0] = view.findViewById(R.id.dot1);
        dots[1] = view.findViewById(R.id.dot2);
        dots[2] = view.findViewById(R.id.dot3);
        dots[3] = view.findViewById(R.id.dot4);

        if (targetSpace != null) {
            tvAuthTitle.setText(targetSpace.getName());
            tvAuthSubtitle.setText("Enter PIN to unlock space");
        } else {
            tvAuthTitle.setText("Authentication");
            tvAuthSubtitle.setText("Enter Master PIN");
        }

        setupKeypad(view);

        MaterialButton btnBiometric = view.findViewById(R.id.btnBiometric);
        SecurityManager sec = SecurityManager.getInstance(requireContext());
        if (sec.isBiometricAvailable(requireContext()) && sec.isBiometricEnabled()) {
            btnBiometric.setVisibility(View.VISIBLE);
            btnBiometric.setOnClickListener(v -> triggerBiometricPrompt());
            // Automatically prompt biometric on launch
            triggerBiometricPrompt();
        } else {
            btnBiometric.setVisibility(View.INVISIBLE);
        }

        view.findViewById(R.id.btnAuthCancel).setOnClickListener(v -> {
            if (callback != null) callback.onCanceled();
            dismiss();
        });
    }

    private void setupKeypad(View root) {
        int[] buttonIds = {
            R.id.btnKey0, R.id.btnKey1, R.id.btnKey2, R.id.btnKey3, R.id.btnKey4,
            R.id.btnKey5, R.id.btnKey6, R.id.btnKey7, R.id.btnKey8, R.id.btnKey9
        };

        for (int i = 0; i < buttonIds.length; i++) {
            final String digit = String.valueOf(i);
            root.findViewById(buttonIds[i]).setOnClickListener(v -> {
                HapticUtils.performHaptic(v);
                appendDigit(digit);
            });
        }

        root.findViewById(R.id.btnKeyBackspace).setOnClickListener(v -> {
            HapticUtils.performHaptic(v);
            removeDigit();
        });
    }

    private void appendDigit(String digit) {
        if (enteredPin.length() < 4) {
            enteredPin.append(digit);
            updateDots();

            if (enteredPin.length() == 4) {
                verifyPin();
            }
        }
    }

    private void removeDigit() {
        if (enteredPin.length() > 0) {
            enteredPin.deleteCharAt(enteredPin.length() - 1);
            updateDots();
        }
    }

    private void updateDots() {
        for (int i = 0; i < 4; i++) {
            if (i < enteredPin.length()) {
                dots[i].setBackgroundResource(R.drawable.bg_pin_dot_filled);
            } else {
                dots[i].setBackgroundResource(R.drawable.bg_pin_dot_empty);
            }
        }
    }

    private void verifyPin() {
        SecurityManager sec = SecurityManager.getInstance(requireContext());
        if (sec.isLockedOut()) {
            long remaining = sec.getLockoutRemainingSeconds();
            Toast.makeText(requireContext(), "Too many failed attempts. Try again in " + remaining + "s", Toast.LENGTH_SHORT).show();
            clearPinWithShake();
            return;
        }

        boolean isValid;
        if (targetSpace != null) {
            isValid = sec.verifySpacePin(targetSpace, enteredPin.toString());
        } else {
            isValid = sec.verifyMasterPin(enteredPin.toString());
        }

        if (isValid) {
            HapticUtils.performHaptic(llPinDots);
            if (callback != null) callback.onAuthenticated();
            dismiss();
        } else {
            Toast.makeText(requireContext(), "Incorrect PIN", Toast.LENGTH_SHORT).show();
            clearPinWithShake();
        }
    }

    private void clearPinWithShake() {
        enteredPin.setLength(0);
        updateDots();

        llPinDots.animate()
                .translationXBy(20f)
                .setDuration(50)
                .withEndAction(() -> llPinDots.animate()
                        .translationXBy(-40f)
                        .setDuration(100)
                        .withEndAction(() -> llPinDots.animate().translationX(0f).setDuration(50).start())
                        .start())
                .start();
    }

    private void triggerBiometricPrompt() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock " + (targetSpace != null ? targetSpace.getName() : "FridgeWise"))
                .setSubtitle("Use your fingerprint or face to authenticate")
                .setNegativeButtonText("Use PIN")
                .build();

        BiometricPrompt prompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(requireContext()),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        SecurityManager sec = SecurityManager.getInstance(requireContext());
                        if (targetSpace != null) {
                            sec.unlockSpaceSession(targetSpace.getId());
                        }
                        if (callback != null) callback.onAuthenticated();
                        dismiss();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        // User chose "Use PIN" or canceled biometric
                    }
                });

        prompt.authenticate(promptInfo);
    }
}
