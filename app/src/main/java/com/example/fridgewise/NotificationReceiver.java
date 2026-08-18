package com.example.fridgewise;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int id = intent.getIntExtra("id", 0);

        NotificationHelper.createNotificationChannel(context);
        NotificationHelper.showNotification(context, title != null ? title : "FridgeWise Alert", 
                message != null ? message : "Something is expiring soon!", id);
    }
}