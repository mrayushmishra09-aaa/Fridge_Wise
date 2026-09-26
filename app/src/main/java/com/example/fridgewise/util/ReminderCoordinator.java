package com.example.fridgewise.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.fridgewise.data.AppDatabase;
import com.example.fridgewise.model.FoodItem;
import com.example.fridgewise.model.NotificationInteraction;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderCoordinator {
    private static final String TAG = "ReminderCoordinator";
    private static final int NOTIFICATION_BUDGET_PER_HOUR = 5;
    
    private final Context context;
    private final ExecutorService executor;

    public ReminderCoordinator(Context context) {
        this(context, Executors.newSingleThreadExecutor());
    }

    public ReminderCoordinator(Context context, ExecutorService executor) {
        this.context = context.getApplicationContext();
        this.executor = executor;
    }

    public void schedule(String type, int itemId, String title, String message, long timeInMillis, int iconResId, String groupKey) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            
            // Log the 'SCHEDULED' intent internally if we wanted, but let's focus on the budget
            long oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000);
            int count = db.notificationInteractionDao().getNotificationCountSince(oneHourAgo);
            
            if (count >= NOTIFICATION_BUDGET_PER_HOUR) {
                Log.w(TAG, "Notification budget exceeded. Delaying reminder.");
                // In a real "Next Level" system, we'd queue this. For now, we'll let it pass or adjust.
            }

            // Get interaction history to decide follow-up behavior
            List<NotificationInteraction> dismissals = db.notificationInteractionDao().getDismissalsForItem(type, itemId);
            int dismissCount = dismissals.size();

            // Calculate adaptive time if needed (e.g. if user ignores, push it further)
            long finalTime = timeInMillis;
            if (dismissCount > 0) {
                // Example: Add 10 mins for every dismissal
                // finalTime += (dismissCount * 10 * 60 * 1000);
            }

            scheduleAlarm(type, itemId, title, message, finalTime, iconResId, groupKey, dismissCount);
        });
    }

    public void logInteraction(String type, int itemId, String action) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            db.notificationInteractionDao().insert(new NotificationInteraction(type, itemId, action, System.currentTimeMillis()));
        });
    }

    private void scheduleAlarm(String type, int itemId, String title, String message, long timeInMillis, int iconResId, String groupKey, int dismissCount) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        
        // Humanize title/message using Personality Engine
        String humanTitle = NotificationPersonalityEngine.getHumanizedMessage(type.toLowerCase(), dismissCount > 0, dismissCount);
        
        intent.putExtra("title", humanTitle);
        intent.putExtra("message", message);
        intent.putExtra("id", generateNotificationId(type, itemId));
        intent.putExtra("iconResId", iconResId);
        intent.putExtra("actionType", type);
        intent.putExtra("groupKey", groupKey);
        intent.putExtra("item_id_actual", itemId);

        if ("FOOD".equals(type)) {
            AppDatabase db = AppDatabase.getInstance(context);
            FoodItem food = db.foodItemDao().getItemById(itemId);
            if (food != null) {
                intent.putExtra("item_name", food.getName());
                intent.putExtra("item_unit", food.getUnit());
                intent.putExtra("item_qty", food.getQuantity());
            }
        }

        int id = generateNotificationId(type, itemId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            }
        }
    }

    private int generateNotificationId(String type, int itemId) {
        if ("MEDICINE".equals(type)) return 10000 + itemId;
        if ("TODO".equals(type)) return 20000 + itemId;
        if ("FOOD".equals(type)) return 30000 + itemId;
        return 40000 + itemId;
    }
}
