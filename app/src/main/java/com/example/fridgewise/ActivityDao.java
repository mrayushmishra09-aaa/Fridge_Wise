package com.example.fridgewise;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ActivityDao {
    @Insert
    void insert(ActivityRecord record);

    @Query("SELECT * FROM activity_records ORDER BY timestamp DESC LIMIT :limit")
    List<ActivityRecord> getRecentActivities(int limit);

    @Query("DELETE FROM activity_records")
    void clearAll();
}
