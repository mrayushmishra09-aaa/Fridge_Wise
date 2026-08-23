package com.example.fridgewise;

import androidx.room.Dao;
import androidx.room.Update;
import androidx.room.Insert;
import androidx.room.Delete;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FoodItemDao {
    @Insert
    long insert(FoodItem item);

    @Update
    void update(FoodItem item);

    @Delete
    void delete(FoodItem item);

    @Query("SELECT * FROM food_items ORDER BY expiryDate ASC")
    List<FoodItem> getAllItems();
}
