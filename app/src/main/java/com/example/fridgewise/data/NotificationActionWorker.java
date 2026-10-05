package com.example.fridgewise.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.fridgewise.R;
import com.example.fridgewise.model.ActivityRecord;
import com.example.fridgewise.model.MedicineEntity;
import com.example.fridgewise.model.ShoppingItem;
import com.example.fridgewise.model.TodoItem;
import com.example.fridgewise.util.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationActionWorker extends Worker {

    public NotificationActionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        String action = getInputData().getString("action");
        int notificationId = getInputData().getInt("id", 0);
        int actualItemId = getInputData().getInt("item_id_actual", 0);

        AppDatabase db = AppDatabase.getInstance(context);

        if (NotificationHelper.ACTION_TAKE_DOSE.equals(action)) {
            handleTakeDose(db, actualItemId, notificationId, context);
        } else if (NotificationHelper.ACTION_MARK_TODO_DONE.equals(action)) {
            handleMarkTodoDone(db, actualItemId, notificationId, context);
        } else if (NotificationHelper.ACTION_ADD_TO_SHOPPING.equals(action)) {
            String name = getInputData().getString("item_name");
            String unit = getInputData().getString("item_unit");
            String qty = getInputData().getString("item_qty");
            handleAddToShopping(db, name, unit, qty, notificationId, context);
        }

        return Result.success();
    }

    private void handleMarkTodoDone(AppDatabase db, int actualId, int notificationId, Context context) {
        TodoItem target = db.todoDao().getTodoById(actualId);
        if (target != null) {
            target.setCompleted(true);
            db.todoDao().update(target);
            db.activityDao().insert(new ActivityRecord("Tasks", "Completed (via Notify)", target.getTitle(), System.currentTimeMillis(), R.drawable.ic_todo_item));
            
            NotificationManagerCompat.from(context).cancel(notificationId);
            showToast(context, "Task marked as completed");
        }
    }

    private void handleAddToShopping(AppDatabase db, String name, String unit, String qty, int notificationId, Context context) {
        ShoppingItem item = new ShoppingItem(name != null ? name : "Expired Item", qty, unit, false);
        db.shoppingDao().insert(item);
        db.activityDao().insert(new ActivityRecord("Shopping List", "Added (via Notify)", name, System.currentTimeMillis(), R.drawable.v02_img_icons_shopping));

        NotificationManagerCompat.from(context).cancel(notificationId);
        showToast(context, "Added " + name + " to shopping list");
    }

    private void handleTakeDose(AppDatabase db, int actualId, int notificationId, Context context) {
        MedicineEntity med = db.medicineDao().getMedicineById(actualId);
        if (med != null) {
            String today = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());
            med.setLastTakenDate(today);

            try {
                double currentQty = Double.parseDouble(med.getQuantity());
                double dosage = Double.parseDouble(med.getDosage());
                if (currentQty >= dosage) {
                    double remaining = currentQty - dosage;
                    if (remaining == (long) remaining) {
                        med.setQuantity(String.valueOf((long) remaining));
                    } else {
                        med.setQuantity(String.valueOf(remaining));
                    }
                }
            } catch (Exception ignored) {}

            db.medicineDao().update(med);
            db.activityDao().insert(new ActivityRecord("Medicine", "Dose Taken (via Notify)", med.getMedicineName(), System.currentTimeMillis(), med.getIconResId()));

            NotificationManagerCompat.from(context).cancel(notificationId);
            showToast(context, "Dose logged for " + med.getMedicineName());
        }
    }

    private void showToast(Context context, String message) {
        new Handler(Looper.getMainLooper()).post(() -> 
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }
}
