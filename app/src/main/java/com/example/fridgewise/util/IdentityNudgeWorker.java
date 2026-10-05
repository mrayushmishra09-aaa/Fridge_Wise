package com.example.fridgewise.util;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.fridgewise.R;
import com.example.fridgewise.data.PreferenceManager;

/**
 * Worker that sends a professional nudge to Guest users to sign in.
 */
public class IdentityNudgeWorker extends Worker {

    public IdentityNudgeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        PreferenceManager prefManager = new PreferenceManager(context);

        if (prefManager.isGuest() && !prefManager.isLoggedIn()) {
            String title = "Secure Your Fridge";
            String message = NotificationPersonalityEngine.getSecurityNudge();
            
            NotificationHelper.showNotification(
                    context,
                    title,
                    message,
                    999, // Unique ID for security nudge
                    R.drawable.ic_sparkle,
                    null,
                    null,
                    null,
                    "IDENTITY",
                    null,
                    0
            );
        }

        return Result.success();
    }
}
