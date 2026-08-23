package com.example.fridgewise;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    public static final String CHANNEL_ID = "expiry_alerts_channel";
    public static final String CHANNEL_REMINDERS = "reminders_channel";
    public static final String CHANNEL_GENERAL = "general_channel";

    public static final String ACTION_TAKE_DOSE = "com.example.fridgewise.ACTION_TAKE_DOSE";
    public static final String ACTION_ADD_TO_SHOPPING = "com.example.fridgewise.ACTION_ADD_TO_SHOPPING";
    public static final String ACTION_MARK_TODO_DONE = "com.example.fridgewise.ACTION_MARK_TODO_DONE";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager == null) return;

            // 1. Reminders Channel (High Importance - Sound/Vibrate)
            NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_REMINDERS,
                    "Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            reminderChannel.setDescription("Medicine and Task reminders");
            notificationManager.createNotificationChannel(reminderChannel);

            // 2. General Channel (Default Importance)
            NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_GENERAL,
                    "General Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            generalChannel.setDescription("Food expiry and stock alerts");
            notificationManager.createNotificationChannel(generalChannel);
            
            // Cleanup old channel if necessary
            notificationManager.deleteNotificationChannel(CHANNEL_ID);
        }
    }

    public static void showNotification(Context context, String title, String message, int notificationId, int iconResId, 
                                        Intent actionIntent, String actionText, PendingIntent contentIntent, String actionType, String groupKey) {
        
        String channelId = ("MEDICINE".equals(actionType) || "TODO".equals(actionType)) ? CHANNEL_REMINDERS : CHANNEL_GENERAL;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.notify_img)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // Support long notes
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        if (groupKey != null) {
            builder.setGroup(groupKey);
        }

        // Add action button if provided
        if (actionIntent != null && actionText != null) {
            PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, notificationId, actionIntent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(0, actionText, actionPendingIntent);
        }

        // If a specific category icon is provided, show it as the large icon
        if (iconResId != 0) {
            try {
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeResource(context.getResources(), iconResId);
                if (bitmap != null) {
                    builder.setLargeIcon(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    public static void scheduleNotification(Context context, long timeInMillis, String title, String message, int id, int iconResId, String actionType, Bundle extras, String groupKey) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("id", id);
        intent.putExtra("iconResId", iconResId);
        intent.putExtra("actionType", actionType);
        intent.putExtra("groupKey", groupKey);
        if (extras != null) {
            intent.putExtras(extras);
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, flags);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                // Fallback to non-exact alarm if permission not granted on API 31+
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                Log.d("NotificationHelper", "Permission not granted for exact alarms, using setAndAllowWhileIdle instead.");
            } else {
                // For API < 31 or if permission is granted on API 31+
                // minSdk is 24 (Nougat), so setExactAndAllowWhileIdle (API 23+) is always available
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            }
            Log.d("NotificationHelper", "Scheduled notification for: " + timeInMillis);
        }
    }

    public static void cancelNotification(Context context, int id) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, flags);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
