package com.example.fridgewise.util;

import android.view.HapticFeedbackConstants;
import android.view.View;

import com.example.fridgewise.data.PreferenceManager;

public class HapticUtils {
    public static void performHaptic(View view) {
        if (view == null) return;
        try {
            PreferenceManager prefManager = new PreferenceManager(view.getContext());
            if (prefManager.isHapticFeedbackEnabled()) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
