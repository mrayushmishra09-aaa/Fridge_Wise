package com.example.fridgewise.util;

import android.content.Context;
import android.util.Log;

import com.example.fridgewise.data.AppDatabase;
import com.example.fridgewise.model.CustomSpace;
import com.example.fridgewise.model.CustomSpaceItem;
import com.example.fridgewise.model.DocumentItem;
import com.example.fridgewise.model.FoodItem;
import com.example.fridgewise.model.MedicineEntity;
import com.example.fridgewise.model.ShoppingItem;
import com.example.fridgewise.model.TodoItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

public class BackupManager {
    private static final String TAG = "BackupManager";

    public static class DatabaseBackup {
        public List<FoodItem> foodItems;
        public List<TodoItem> todoItems;
        public List<MedicineEntity> medicines;
        public List<ShoppingItem> shoppingItems;
        public List<DocumentItem> documents;
        public List<CustomSpace> customSpaces;
        public List<CustomSpaceItem> customSpaceItems;
    }

    public static String exportToJson(Context context) {
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            DatabaseBackup backup = new DatabaseBackup();
            backup.foodItems = db.foodItemDao().getAllItems();
            backup.todoItems = db.todoDao().getAllTodos();
            backup.medicines = db.medicineDao().getAllMedicines();
            backup.shoppingItems = db.shoppingDao().getAllItems();
            backup.documents = db.documentDao().getAllDocuments();
            backup.customSpaces = db.customSpaceDao().getAllSpacesSync();
            backup.customSpaceItems = db.customSpaceDao().getAllCustomSpaceItemsSync();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(backup);
        } catch (Exception e) {
            Log.e(TAG, "Error exporting database", e);
            return null;
        }
    }

    public static boolean exportToFile(Context context, File destinationFile) {
        String json = exportToJson(context);
        if (json == null) return false;
        try (FileWriter writer = new FileWriter(destinationFile)) {
            writer.write(json);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error writing backup file", e);
            return false;
        }
    }

    public static boolean restoreFromFile(Context context, File sourceFile) {
        try (FileReader reader = new FileReader(sourceFile)) {
            Gson gson = new Gson();
            DatabaseBackup backup = gson.fromJson(reader, DatabaseBackup.class);
            if (backup == null) return false;

            AppDatabase db = AppDatabase.getInstance(context);
            if (backup.foodItems != null) {
                for (FoodItem item : backup.foodItems) {
                    db.foodItemDao().insert(item);
                }
            }
            if (backup.todoItems != null) {
                for (TodoItem item : backup.todoItems) {
                    db.todoDao().insert(item);
                }
            }
            if (backup.medicines != null) {
                for (MedicineEntity item : backup.medicines) {
                    db.medicineDao().insert(item);
                }
            }
            if (backup.shoppingItems != null) {
                for (ShoppingItem item : backup.shoppingItems) {
                    db.shoppingDao().insert(item);
                }
            }
            if (backup.documents != null) {
                for (DocumentItem item : backup.documents) {
                    db.documentDao().insert(item);
                }
            }
            if (backup.customSpaces != null) {
                for (CustomSpace item : backup.customSpaces) {
                    db.customSpaceDao().insertSpace(item);
                }
            }
            if (backup.customSpaceItems != null) {
                for (CustomSpaceItem item : backup.customSpaceItems) {
                    db.customSpaceDao().insertItem(item);
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error restoring backup", e);
            return false;
        }
    }
}
