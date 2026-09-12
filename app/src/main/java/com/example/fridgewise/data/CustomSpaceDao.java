package com.example.fridgewise.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.fridgewise.model.CustomSpace;
import com.example.fridgewise.model.CustomSpaceItem;
import java.util.List;

@Dao
public interface CustomSpaceDao {
    @Insert
    long insertSpace(CustomSpace space);

    @Update
    void updateSpace(CustomSpace space);

    @Delete
    void deleteSpace(CustomSpace space);

    @Query("SELECT * FROM custom_spaces")
    LiveData<List<CustomSpace>> getAllSpaces();

    @Query("SELECT * FROM custom_spaces WHERE id = :id")
    LiveData<CustomSpace> getSpaceById(int id);

    @Insert
    long insertItem(CustomSpaceItem item);

    @Update
    void updateItem(CustomSpaceItem item);

    @Delete
    void deleteItem(CustomSpaceItem item);

    @Query("SELECT * FROM custom_space_items WHERE spaceId = :spaceId")
    LiveData<List<CustomSpaceItem>> getItemsForSpace(int spaceId);

    @Query("SELECT * FROM custom_space_items")
    List<CustomSpaceItem> getAllCustomSpaceItemsSync();

    @Query("SELECT COUNT(*) FROM custom_space_items WHERE spaceId = :spaceId")
    int getItemCountForSpace(int spaceId);
}
