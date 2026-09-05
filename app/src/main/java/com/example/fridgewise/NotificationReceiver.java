package com.example.fridgewise;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int id = intent.getIntExtra("id", 0);

        if (NotificationHelper.ACTION_TAKE_DOSE.equals(action)) {
            handleTakeDose(context, id);
            return;
        } else if (NotificationHelper.ACTION_ADD_TO_SHOPPING.equals(action)) {
            handleAddToShopping(context, intent);
            return;
        } else if (NotificationHelper.ACTION_MARK_TODO_DONE.equals(action)) {
            handleMarkTodoDone(context, id);
            return;
        }

        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int iconResId = intent.getIntExtra("iconResId", 0);
        String actionType = intent.getStringExtra("actionType");
        String groupKey = intent.getStringExtra("groupKey");

        // Schedule Smart Follow-up if enabled
        PreferenceManager pref = new PreferenceManager(context);
        if (pref.isSmartFollowUpEnabled() && ("MEDICINE".equals(actionType) || "TODO".equals(actionType))) {
            scheduleSmartFollowUp(context, id, actionType, title, iconResId);
        }

        Intent customActionIntent = null;
        String actionText = null;

        if ("MEDICINE".equals(actionType)) {
            customActionIntent = new Intent(context, NotificationReceiver.class);
            customActionIntent.setAction(NotificationHelper.ACTION_TAKE_DOSE);
            customActionIntent.putExtra("id", id);
            actionText = "Take Dose";
        } else if ("FOOD".equals(actionType)) {
            customActionIntent = new Intent(context, NotificationReceiver.class);
            customActionIntent.setAction(NotificationHelper.ACTION_ADD_TO_SHOPPING);
            customActionIntent.putExtra("id", id);
            customActionIntent.putExtra("item_name", intent.getStringExtra("item_name"));
            customActionIntent.putExtra("item_unit", intent.getStringExtra("item_unit"));
            customActionIntent.putExtra("item_qty", intent.getStringExtra("item_qty"));
            actionText = "Add to Shopping";
        } else if ("TODO".equals(actionType)) {
            customActionIntent = new Intent(context, NotificationReceiver.class);
            customActionIntent.setAction(NotificationHelper.ACTION_MARK_TODO_DONE);
            customActionIntent.putExtra("id", id);
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
                customActionIntent, actionText, contentPendingIntent, actionType, groupKey);
    }

    private void handleMarkTodoDone(Context context, int todoId) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            int actualId = todoId - 20000;
            TodoItem target = db.todoDao().getTodoById(actualId);
            if (target != null) {
                target.setCompleted(true);
                db.todoDao().update(target);
                // LOG ACTIVITY
                db.activityDao().insert(new ActivityRecord("Tasks", "Completed (via Notify)", target.getTitle(), System.currentTimeMillis(), R.drawable.ic_todo_item));
                
                NotificationManagerCompat.from(context).cancel(todoId);
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "Task marked as completed", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void handleAddToShopping(Context context, Intent intent) {
        String name = intent.getStringExtra("item_name");
        String unit = intent.getStringExtra("item_unit");
        String qty = intent.getStringExtra("item_qty");
        int notificationId = intent.getIntExtra("id", 0);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            ShoppingItem item = new ShoppingItem(name != null ? name : "Expired Item", qty, unit, false);
            db.shoppingDao().insert(item);
            
            // LOG ACTIVITY
            db.activityDao().insert(new ActivityRecord("Shopping List", "Added (via Notify)", name, System.currentTimeMillis(), R.drawable.v02_img_icons_shopping));

            NotificationManagerCompat.from(context).cancel(notificationId);
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(context, "Added " + name + " to shopping list", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void handleTakeDose(Context context, int medicineId) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            int actualId = medicineId - 10000;
            MedicineEntity med = db.medicineDao().getMedicineById(actualId);
            if (med != null) {
                // Update taken date
                String today = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());
                med.setLastTakenDate(today);

                // Update quantity
                try {
                    double currentQty = Double.parseDouble(med.getQuantity());
                    double dosage = Double.parseDouble(med.getDosage());
                    if (currentQty >= dosage) {
                        double remaining = currentQty - dosage;
                        // Format to remove .0 if it's a whole number
                        if (remaining == (long) remaining) {
                            med.setQuantity(String.valueOf((long) remaining));
                        } else {
                            med.setQuantity(String.valueOf(remaining));
                        }
                    }
                } catch (Exception e) {}

                db.medicineDao().update(med);

                // LOG ACTIVITY
                db.activityDao().insert(new ActivityRecord("Medicine", "Dose Taken (via Notify)", med.getMedicineName(), System.currentTimeMillis(), med.getIconResId()));

                // Dismiss notification
                NotificationManagerCompat.from(context).cancel(medicineId);
                
                // Show toast (must be on main thread)
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "Dose logged for " + med.getMedicineName(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void scheduleSmartFollowUp(Context context, int id, String type, String name, int iconRes) {
        Data inputData = new Data.Builder()
                .putInt("id", id)
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