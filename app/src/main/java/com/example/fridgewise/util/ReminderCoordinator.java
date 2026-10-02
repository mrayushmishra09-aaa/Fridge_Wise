package com.example.fridgewise.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.fridgewise.data.AppDatabase;
import com.example.fridgewise.model.FoodItem;
import com.example.fridgewise.model.NotificationInteraction;

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
        schedule3StageReminders(type, itemId, title, message, timeInMillis, iconResId, groupKey, false, 0, 0);
    }

    public void schedule3StageReminders(String type, int itemId, String title, String message, long targetTimeInMillis, int iconResId, String groupKey, boolean isClockAlarmEnabled, int preOffsetMins, int postOffsetMins) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();

            // Stage 1: Pre-Notification (Advance Warning)
            if (preOffsetMins > 0) {
                long preTime = targetTimeInMillis - (preOffsetMins * 60L * 1000L);
                if (preTime > now) {
                    String preTitle = "⏳ Upcoming: " + title;
                    String preMsg = "Reminder: " + message + " (due in " + preOffsetMins + " mins)";
                    int preId = generateNotificationId(type, itemId) + 100000;
                    dispatchAlarm(type, itemId, preId, preTitle, preMsg, preTime, iconResId, groupKey, false);
                }
            }

            // Stage 2: Target Trigger / Alarm
            if (targetTimeInMillis > now) {
                int mainId = generateNotificationId(type, itemId);
                dispatchAlarm(type, itemId, mainId, title, message, targetTimeInMillis, iconResId, groupKey, isClockAlarmEnabled);
            }

            // Stage 3: Post-Notification (Missed Target Follow-up)
            if (postOffsetMins > 0) {
                long postTime = targetTimeInMillis + (postOffsetMins * 60L * 1000L);
                if (postTime > now) {
                    String postTitle = "❓ Missed Target: " + title;
                    String postMsg = "Did you complete '" + title + "'? Tap to mark completed or reschedule.";
                    int postId = generateNotificationId(type, itemId) + 200000;
                    dispatchAlarm(type, itemId, postId, postTitle, postMsg, postTime, iconResId, groupKey, false);
                }
            }
        });
    }

    public void logInteraction(String type, int itemId, String action) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            db.notificationInteractionDao().insert(new NotificationInteraction(type, itemId, action, System.currentTimeMillis()));
        });
    }

    private void dispatchAlarm(String type, int itemId, int notificationId, String title, String message, long timeInMillis, int iconResId, String groupKey, boolean isLoudAlarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("id", notificationId);
        intent.putExtra("iconResId", iconResId);
        intent.putExtra("actionType", type);
        intent.putExtra("groupKey", groupKey);
        intent.putExtra("item_id_actual", itemId);
        intent.putExtra("isLoudAlarm", isLoudAlarm);

        if ("FOOD".equals(type)) {
            AppDatabase db = AppDatabase.getInstance(context);
            FoodItem food = db.foodItemDao().getItemById(itemId);
            if (food != null) {
                intent.putExtra("item_name", food.getName());
                intent.putExtra("item_unit", food.getUnit());
                intent.putExtra("item_qty", food.getQuantity());
            }
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            if (isLoudAlarm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Intent showIntent = new Intent(context, NotificationReceiver.class);
                PendingIntent showPendingIntent = PendingIntent.getBroadcast(context, notificationId + 1, showIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(timeInMillis, showPendingIntent);
                alarmManager.setAlarmClock(clockInfo, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
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
        if ("CUSTOM".equals(type)) return 40000 + itemId;
        return 50000 + itemId;
    }
}
