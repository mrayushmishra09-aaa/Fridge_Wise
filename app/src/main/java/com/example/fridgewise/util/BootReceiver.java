package com.example.fridgewise.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.fridgewise.R;
import com.example.fridgewise.data.AppDatabase;
import com.example.fridgewise.model.CustomSpaceItem;
import com.example.fridgewise.model.FoodItem;
import com.example.fridgewise.model.MedicineEntity;
import com.example.fridgewise.model.TodoItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device rebooted, rescheduling all reminders...");
            rescheduleAll(context);
        }
    }

    private void rescheduleAll(Context context) {
        ReminderCoordinator coordinator = new ReminderCoordinator(context);
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);

            // 1. Medicines
            List<MedicineEntity> medicines = db.medicineDao().getAllMedicines();
            if (medicines != null) {
                for (MedicineEntity med : medicines) {
                    if (med.isReminderOn()) {
                        long time = parseDateTime(med.getStartDate(), med.getStartTime(), "d/M/yyyy hh:mm a");
                        if (time > System.currentTimeMillis()) {
                            coordinator.schedule("MEDICINE", med.getId(), "Medicine Reminder", "Time for " + med.getMedicineName(), time, med.getIconResId(), "group_meds");
                        }
                    }
                }
            }

            // 2. Todos
            List<TodoItem> todos = db.todoDao().getPendingTodos();
            if (todos != null) {
                for (TodoItem todo : todos) {
                    if (todo.isReminderSet()) {
                        long time = parseDateTime(todo.getDate(), todo.getTime(), "d/M/yyyy HH:mm");
                        if (time > System.currentTimeMillis()) {
                            coordinator.schedule("TODO", todo.getId(), "Task Reminder", todo.getTitle(), time, CategoryUtils.getPriorityIcon(todo.getPriority()), "group_todo");
                        }
                    }
                }
            }

            // 3. Custom Space Items
            List<CustomSpaceItem> customItems = db.customSpaceDao().getAllCustomSpaceItemsSync();
            if (customItems != null) {
                for (CustomSpaceItem item : customItems) {
                    if (item.getReminderTimestamp() != null && item.getReminderTimestamp() > System.currentTimeMillis()) {
                        coordinator.schedule("SPACE", item.getId(), "Space Reminder", item.getName(), item.getReminderTimestamp(), R.drawable.ic_sparkle, "group_space");
                    }
                }
            }

            // 4. Food Expiry
            List<FoodItem> foodItems = db.foodItemDao().getAllItems();
            if (foodItems != null) {
                for (FoodItem food : foodItems) {
                    if (food.getExpiryTimestamp() > System.currentTimeMillis()) {
                        coordinator.schedule("FOOD", food.getId(), "Food Expiry", food.getName() + " is expiring soon!", food.getExpiryTimestamp(), R.drawable.notify_img, "group_food");
                    }
                }
            }

        }).start();
    }

    private long parseDateTime(String date, String time, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
            Date d = sdf.parse(date + " " + time);
            return d != null ? d.getTime() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
