package com.example.fridgewise.data;

import androidx.room.Dao;
import androidx.room.Update;
import androidx.room.Insert;
import androidx.room.Delete;
import androidx.room.Query;
import com.example.fridgewise.model.FoodItem;
import java.util.List;

@Dao
public interface FoodItemDao {
    @Insert
    long insert(FoodItem item);

    @Update
    void update(FoodItem item);

    @Delete
    void delete(FoodItem item);

    @Query("SELECT * FROM food_items ORDER BY expiryTimestamp ASC")
    List<FoodItem> getAllItems();

    @Query("SELECT * FROM food_items WHERE id = :id")
    FoodItem getItemById(int id);
}
