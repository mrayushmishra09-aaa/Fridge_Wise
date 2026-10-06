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
import androidx.fragment.app.DialogFragment;

import com.example.fridgewise.R;
import com.example.fridgewise.util.HapticUtils;
import com.example.fridgewise.util.SecurityManager;

public class SetupPinDialog extends DialogFragment {

    public interface SetupPinCallback {
        void onPinCreated(String pin, String salt, String hash);
        void onCanceled();
    }

    private SetupPinCallback callback;
    private String titleText = "Set Master PIN";

    private String firstPin = null;
    private final StringBuilder currentPin = new StringBuilder();

    private TextView tvSetupSubtitle;
    private View llSetupPinDots;
    private final View[] dots = new View[4];

    public static SetupPinDialog newInstance(String title, SetupPinCallback callback) {
        SetupPinDialog dialog = new SetupPinDialog();
        if (title != null) dialog.titleText = title;
        dialog.callback = callback;
        return dialog;
    }

    public static SetupPinDialog newInstance(SetupPinCallback callback) {
        return newInstance("Set Master PIN", callback);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.dialog_setup_pin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvSetupTitle = view.findViewById(R.id.tvSetupTitle);
        tvSetupSubtitle = view.findViewById(R.id.tvSetupSubtitle);
        llSetupPinDots = view.findViewById(R.id.llSetupPinDots);

        dots[0] = view.findViewById(R.id.setupDot1);
        dots[1] = view.findViewById(R.id.setupDot2);
        dots[2] = view.findViewById(R.id.setupDot3);
        dots[3] = view.findViewById(R.id.setupDot4);

        tvSetupTitle.setText(titleText);
        tvSetupSubtitle.setText("Enter a 4-digit PIN");

        setupKeypad(view);

        view.findViewById(R.id.btnSetupCancel).setOnClickListener(v -> {
            if (callback != null) callback.onCanceled();
            dismiss();
        });
    }

    private void setupKeypad(View root) {
        int[] buttonIds = {
                R.id.btnSetupKey0, R.id.btnSetupKey1, R.id.btnSetupKey2, R.id.btnSetupKey3, R.id.btnSetupKey4,
                R.id.btnSetupKey5, R.id.btnSetupKey6, R.id.btnSetupKey7, R.id.btnSetupKey8, R.id.btnSetupKey9
        };

        for (int i = 0; i < buttonIds.length; i++) {
            final String digit = String.valueOf(i);
            root.findViewById(buttonIds[i]).setOnClickListener(v -> {
                HapticUtils.performHaptic(v);
                appendDigit(digit);
            });
        }

        root.findViewById(R.id.btnSetupKeyBackspace).setOnClickListener(v -> {
            HapticUtils.performHaptic(v);
            removeDigit();
        });
    }

    private void appendDigit(String digit) {
        if (currentPin.length() < 4) {
            currentPin.append(digit);
            updateDots();

            if (currentPin.length() == 4) {
                onFourDigitsEntered();
            }
        }
    }

    private void removeDigit() {
        if (currentPin.length() > 0) {
            currentPin.deleteCharAt(currentPin.length() - 1);
            updateDots();
        }
    }

    private void updateDots() {
        for (int i = 0; i < 4; i++) {
            if (i < currentPin.length()) {
                dots[i].setBackgroundResource(R.drawable.bg_pin_dot_filled);
            } else {
                dots[i].setBackgroundResource(R.drawable.bg_pin_dot_empty);
            }
        }
    }

    private void onFourDigitsEntered() {
        if (firstPin == null) {
            // First step complete -> move to confirmation step
            firstPin = currentPin.toString();
            currentPin.setLength(0);
            updateDots();
            tvSetupSubtitle.setText("Confirm your 4-digit PIN");
        } else {
            // Confirmation step complete -> verify match
            String confirmPin = currentPin.toString();
            if (firstPin.equals(confirmPin)) {
                SecurityManager sec = SecurityManager.getInstance(requireContext());
                String salt = sec.createSaltForSpace();
                String hash = sec.hashPinForSpace(confirmPin, salt);

                HapticUtils.performHaptic(llSetupPinDots);
                if (callback != null) {
                    callback.onPinCreated(confirmPin, salt, hash);
                }
                dismiss();
            } else {
                Toast.makeText(requireContext(), "PINs do not match. Try again.", Toast.LENGTH_SHORT).show();
                resetToStepOneWithShake();
            }
        }
    }

    private void resetToStepOneWithShake() {
        firstPin = null;
        currentPin.setLength(0);
        updateDots();
        tvSetupSubtitle.setText("Enter a 4-digit PIN");

        llSetupPinDots.animate()
                .translationXBy(20f)
                .setDuration(50)
                .withEndAction(() -> llSetupPinDots.animate()
                        .translationXBy(-40f)
                        .setDuration(100)
                        .withEndAction(() -> llSetupPinDots.animate().translationX(0f).setDuration(50).start())
                        .start())
                .start();
    }
}
