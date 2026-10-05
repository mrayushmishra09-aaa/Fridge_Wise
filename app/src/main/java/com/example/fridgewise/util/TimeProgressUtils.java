package com.example.fridgewise.util;

import com.example.fridgewise.R;
import java.util.Locale;

public class TimeProgressUtils {

    public static class ProgressState {
        private final String label;
        private final int progressPercent;
        private final int colorResId;
        private final boolean isVisible;

        public ProgressState(String label, int progressPercent, int colorResId, boolean isVisible) {
            this.label = label;
            this.progressPercent = progressPercent;
            this.colorResId = colorResId;
            this.isVisible = isVisible;
        }

        public String getLabel() { return label; }
        public int getProgressPercent() { return progressPercent; }
        public int getColorResId() { return colorResId; }
        public boolean isVisible() { return isVisible; }
    }

    public static ProgressState calculateState(Long targetTimestamp, Long creationTimestamp) {
        if (targetTimestamp == null || targetTimestamp <= 0) {
            return new ProgressState("", 0, R.color.green_primary, false);
        }

        long now = System.currentTimeMillis();
        long diff = targetTimestamp - now;

        if (diff <= 0) {
            return new ProgressState("⏰ Time to start", 100, R.color.red_expired, true);
        }

        // Label calculation
        String label;
        long mins = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);

        if (mins < 60) {
            label = String.format(Locale.getDefault(), "⏱️ %d %s remaining", mins, mins == 1 ? "min" : "mins");
        } else if (hours < 24) {
            label = String.format(Locale.getDefault(), "⏱️ %d %s remaining", hours, hours == 1 ? "hour" : "hours");
        } else {
            label = String.format(Locale.getDefault(), "⏱️ %d %s remaining", days, days == 1 ? "day" : "days");
        }

        // Progress percentage calculation
        long start = (creationTimestamp != null && creationTimestamp > 0 && creationTimestamp < targetTimestamp)
                ? creationTimestamp
                : Math.min(now, targetTimestamp - (60 * 60 * 1000L));

        long total = targetTimestamp - start;
        long elapsed = now - start;
        int percent = 0;
        if (total > 0) {
            percent = (int) Math.min(100, Math.max(0, (elapsed * 100) / total));
        }

        // Color gradient based on percentage segments:
        // 0% - 50%: Green
        // 50% - 85%: Orange
        // 85% - 100%: Red
        int colorRes;
        if (percent < 50) {
            colorRes = R.color.green_primary;
        } else if (percent < 85) {
            colorRes = R.color.orange_warning;
        } else {
            colorRes = R.color.red_expired;
        }

        return new ProgressState(label, percent, colorRes, true);
    }
}
