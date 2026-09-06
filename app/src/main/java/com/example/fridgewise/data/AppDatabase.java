package com.example.fridgewise.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.fridgewise.model.ActivityRecord;
import com.example.fridgewise.model.CustomSpace;
import com.example.fridgewise.model.CustomSpaceItem;
import com.example.fridgewise.model.DocumentItem;
import com.example.fridgewise.model.FoodItem;
import com.example.fridgewise.model.MedicineEntity;
import com.example.fridgewise.model.ShoppingItem;
import com.example.fridgewise.model.TodoItem;

@Database(entities = {FoodItem.class, TodoItem.class, MedicineEntity.class, ShoppingItem.class, DocumentItem.class, CustomSpace.class, CustomSpaceItem.class, ActivityRecord.class}, version = 18, exportSchema = false)
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
                            .addMigrations(MIGRATION_17_18)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_17_18 = new Migration(17, 18) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE food_items ADD COLUMN expiryTimestamp INTEGER NOT NULL DEFAULT 0");
        }
    };
}
