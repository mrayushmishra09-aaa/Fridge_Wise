package com.example.fridgewise;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
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
    List<CustomSpace> getAllSpaces();

    @Query("SELECT * FROM custom_spaces WHERE id = :id")
    CustomSpace getSpaceById(int id);

    @Insert
    long insertItem(CustomSpaceItem item);

    @Update
    void updateItem(CustomSpaceItem item);

    @Delete
    void deleteItem(CustomSpaceItem item);

    @Query("SELECT * FROM custom_space_items WHERE spaceId = :spaceId")
    List<CustomSpaceItem> getItemsForSpace(int spaceId);

    @Query("SELECT COUNT(*) FROM custom_space_items WHERE spaceId = :spaceId")
    int getItemCountForSpace(int spaceId);
}
