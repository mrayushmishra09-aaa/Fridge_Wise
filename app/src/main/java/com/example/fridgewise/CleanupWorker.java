package com.example.fridgewise;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CleanupWorker extends Worker {

    public CleanupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);

        long now = System.currentTimeMillis();
        long twentyFourHours = 24 * 60 * 60 * 1000;
        long fortyTwoHours = 42 * 60 * 60 * 1000;

        // 1. Cleanup Completed Todos (>24h)
        List<TodoItem> allTodos = db.todoDao().getAllTodos();
        for (TodoItem todo : allTodos) {
            if (todo.isCompleted() && (now - todo.getStatusChangeTime() > twentyFourHours)) {
                db.todoDao().delete(todo);
            }
        }

        // 2. Cleanup Taken Medicines (>24h)
        // Note: For medicine, we usually just want to reset the "taken" status,
        // but the user asked to "remove that item". If he meant delete the record:
        List<MedicineEntity> allMeds = db.medicineDao().getAllMedicines();
        for (MedicineEntity med : allMeds) {
            if (med.getLastTakenDate() != null && (now - med.getStatusChangeTime() > twentyFourHours)) {
                // If it's a recurring medicine, we might just want to clear the LastTakenDate.
                // But following user's "remove that item" request:
                db.medicineDao().delete(med);
            }
        }

        // 3. Cleanup Expired Food (>42h) and move to Shopping List
        List<FoodItem> allFood = db.foodItemDao().getAllItems();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        for (FoodItem food : allFood) {
            try {
                Date expiry = dateFormat.parse(food.getExpiryDate());
                if (expiry != null && (now - expiry.getTime() > fortyTwoHours)) {
                    // Move to shopping list first
                    ShoppingItem shoppingItem = new ShoppingItem(food.getName(), String.valueOf(food.getQuantity()), food.getUnit(), false);
                    db.shoppingDao().insert(shoppingItem);
                    
                    // Delete from inventory
                    db.foodItemDao().delete(food);
                }
            } catch (Exception e) {
                // Ignore parse errors
            }
        }

        return Result.success();
    }
}
