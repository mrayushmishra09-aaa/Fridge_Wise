package com.example.fridgewise.util;

import com.example.fridgewise.R;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.fridgewise.R;
import com.example.fridgewise.data.AppDatabase;
import com.example.fridgewise.data.NotificationActionWorker;
import com.example.fridgewise.data.NotificationFollowUpWorker;
import com.example.fridgewise.data.PreferenceManager;
import com.example.fridgewise.model.ActivityRecord;
import com.example.fridgewise.model.MedicineEntity;
import com.example.fridgewise.model.ShoppingItem;
import com.example.fridgewise.model.TodoItem;
import com.example.fridgewise.ui.activities.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationReceiver extends BroadcastReceiver {
    private static long lastNotificationTime = 0;
    private static final long BUNDLING_WINDOW = 2000; // 2 seconds

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int id = intent.getIntExtra("id", 0);
        String actionType = intent.getStringExtra("actionType");
        int actualItemId = intent.getIntExtra("item_id_actual", 0);

        ReminderCoordinator coordinator = new ReminderCoordinator(context);

        if (NotificationHelper.ACTION_TAKE_DOSE.equals(action) || 
            NotificationHelper.ACTION_ADD_TO_SHOPPING.equals(action) || 
            NotificationHelper.ACTION_MARK_TODO_DONE.equals(action)) {
            
            // Log interaction
            String interaction = "CLICKED_" + action.substring(action.lastIndexOf(".") + 1);
            coordinator.logInteraction(actionType, actualItemId, interaction);
            
            // Delegate to Worker for resilience
            Data.Builder dataBuilder = new Data.Builder()
                    .putString("action", action)
                    .putInt("id", id)
                    .putInt("item_id_actual", actualItemId);
            
            if (NotificationHelper.ACTION_ADD_TO_SHOPPING.equals(action)) {
                dataBuilder.putString("item_name", intent.getStringExtra("item_name"));
                dataBuilder.putString("item_unit", intent.getStringExtra("item_unit"));
                dataBuilder.putString("item_qty", intent.getStringExtra("item_qty"));
            }
            
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationActionWorker.class)
                    .setInputData(dataBuilder.build())
                    .build();
            WorkManager.getInstance(context).enqueue(workRequest);
            return;
        } else if (NotificationHelper.ACTION_SNOOZE.equals(action)) {
            coordinator.logInteraction(actionType, actualItemId, "SNOOZED");
            handleSnooze(context, intent);
            return;
        } else if (NotificationHelper.ACTION_DISMISSED.equals(action)) {
            coordinator.logInteraction(actionType, actualItemId, "DISMISSED");
            Log.d("NotificationReceiver", "Notification swiped away: " + id);
            return;
        }

        // Standard delivery
        coordinator.logInteraction(actionType, actualItemId, "DELIVERED");

        // Quiet Hours Check for non-critical alerts
        if ("FOOD".equals(actionType) || "SPACE".equals(actionType)) {
            if (isQuietHours(context)) {
                Log.d("NotificationReceiver", "Quiet Hours active. Silencing alert.");
                return;
            }
        }

        long now = System.currentTimeMillis();
        boolean shouldBundle = (now - lastNotificationTime) < BUNDLING_WINDOW;
        lastNotificationTime = now;

        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int iconResId = intent.getIntExtra("iconResId", 0);
        String groupKey = intent.getStringExtra("groupKey");

        if (shouldBundle) {
            title = "A few things are waiting for you 💭";
            message = "You have multiple reminders to check.";
            // We use a fixed ID for bundled notifications to overwrite
            id = 99999; 
        }

        // Schedule Smart Follow-up if enabled
        PreferenceManager pref = new PreferenceManager(context);
        if (pref.isSmartFollowUpEnabled() && ("MEDICINE".equals(actionType) || "TODO".equals(actionType))) {
            scheduleSmartFollowUp(context, id, actualItemId, actionType, title, iconResId);
        }

        Intent customActionIntent = null;
        String actionText = null;

        if ("MEDICINE".equals(actionType)) {
            customActionIntent = new Intent(context, NotificationReceiver.class);
            customActionIntent.setAction(NotificationHelper.ACTION_TAKE_DOSE);
            customActionIntent.putExtra("id", id);
            customActionIntent.putExtra("actionType", actionType);
            customActionIntent.putExtra("item_id_actual", actualItemId);
            actionText = "Take Dose";
        } else if ("FOOD".equals(actionType)) {
            customActionIntent = new Intent(context, NotificationReceiver.class);
            customActionIntent.setAction(NotificationHelper.ACTION_ADD_TO_SHOPPING);
            customActionIntent.putExtra("id", id);
            customActionIntent.putExtra("actionType", actionType);
            customActionIntent.putExtra("item_id_actual", actualItemId);
            customActionIntent.putExtra("item_name", intent.getStringExtra("item_name"));
            customActionIntent.putExtra("item_unit", intent.getStringExtra("item_unit"));
            customActionIntent.putExtra("item_qty", intent.getStringExtra("item_qty"));
            actionText = "Add to Shopping";
        } else if ("TODO".equals(actionType)) {
            customActionIntent = new Intent(context, NotificationReceiver.class);
            customActionIntent.setAction(NotificationHelper.ACTION_MARK_TODO_DONE);
            customActionIntent.putExtra("id", id);
            customActionIntent.putExtra("actionType", actionType);
            customActionIntent.putExtra("item_id_actual", actualItemId);
            actionText = "Mark as Done";
        }

        // Deep linking content intent
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.putExtra("target_fragment", actionType);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, id, mainIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationHelper.createNotificationChannel(context);
        NotificationHelper.showNotification(context, title != null ? title : "FridgeWise Alert", 
                message != null ? message : "Something is expiring soon!", id, iconResId, 
                customActionIntent, actionText, contentPendingIntent, actionType, groupKey, actualItemId);
    }

    private void handleSnooze(Context context, Intent intent) {
        int id = intent.getIntExtra("id", 0);
        String actionType = intent.getStringExtra("actionType");
        int actualItemId = intent.getIntExtra("item_id_actual", 0);
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int iconResId = intent.getIntExtra("iconResId", 0);
        String groupKey = intent.getStringExtra("groupKey");

        // Schedule again in 30 minutes
        long snoozeTime = System.currentTimeMillis() + (30 * 60 * 1000);
        
        ReminderCoordinator coordinator = new ReminderCoordinator(context);
        coordinator.schedule(actionType, actualItemId, title, message, snoozeTime, iconResId, groupKey);

        NotificationManagerCompat.from(context).cancel(id);
        Toast.makeText(context, "Reminder snoozed for 30 minutes", Toast.LENGTH_SHORT).show();
    }

    private boolean isQuietHours(Context context) {
        PreferenceManager pref = new PreferenceManager(context);
        if (!pref.isQuietHoursEnabled()) return false;
        
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        
        int start = pref.getQuietHoursStart();
        int end = pref.getQuietHoursEnd();

        if (start < end) {
            // e.g. 1 PM to 5 PM
            return hour >= start && hour < end;
        } else {
            // e.g. 10 PM to 7 AM
            return hour >= start || hour < end;
        }
    }

    private void scheduleSmartFollowUp(Context context, int id, int actualItemId, String type, String name, int iconRes) {
        Data inputData = new Data.Builder()
                .putInt("id", id)
                .putInt("item_id_actual", actualItemId)
                .putString("type", type)
                .putString("name", name)
                .putInt("iconRes", iconRes)
                .build();

        OneTimeWorkRequest followUpRequest = new OneTimeWorkRequest.Builder(NotificationFollowUpWorker.class)
                .setInitialDelay(1, TimeUnit.HOURS)
                .setInputData(inputData)
                .addTag("followup_" + id)
                .build();

        WorkManager.getInstance(context).enqueue(followUpRequest);
    }
}
