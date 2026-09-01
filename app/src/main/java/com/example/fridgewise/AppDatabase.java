package com.example.fridgewise;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {FoodItem.class, TodoItem.class, MedicineEntity.class, ShoppingItem.class, DocumentItem.class, CustomSpace.class, CustomSpaceItem.class, ActivityRecord.class}, version = 17, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract FoodItemDao foodItemDao();
    public abstract TodoDao todoDao();
    public abstract MedicineDao medicineDao();
    public abstract ShoppingDao shoppingDao();
    public abstract DocumentDao documentDao();
    public abstract CustomSpaceDao customSpaceDao();
    public abstract ActivityDao activityDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "fridge_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }




}
