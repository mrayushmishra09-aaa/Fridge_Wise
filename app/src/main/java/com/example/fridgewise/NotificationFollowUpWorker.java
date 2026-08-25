package com.example.fridgewise;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationFollowUpWorker extends Worker {

    public NotificationFollowUpWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        int id = getInputData().getInt("id", 0);
        String type = getInputData().getString("type");
        String name = getInputData().getString("name");
        int iconRes = getInputData().getInt("iconRes", 0);

        AppDatabase db = AppDatabase.getInstance(context);

        boolean isPending = false;

        if ("MEDICINE".equals(type)) {
            MedicineEntity med = db.medicineDao().getMedicineById(id);
            if (med != null) {
                String today = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());
                if (!today.equals(med.getLastTakenDate())) {
                    isPending = true;
                }
            }
        } else if ("TODO".equals(type)) {
            int actualId = id - 10000;
            TodoItem todo = null;
            for (TodoItem item : db.todoDao().getAllTodos()) {
                if (item.getId() == actualId) {
                    todo = item;
                    break;
                }
            }
            if (todo != null && !todo.isCompleted()) {
                isPending = true;
            }
        }

        if (isPending) {
            String title = "Gentle Reminder: " + name;
            String message = "Just checking in - did you manage to " + 
                    ("MEDICINE".equals(type) ? "take your medicine?" : "finish this task?");
            
            NotificationHelper.showNotification(context, title, message, id + 500, iconRes, 
                    null, null, null, type, "group_followup");
        }

        return Result.success();
    }
}
