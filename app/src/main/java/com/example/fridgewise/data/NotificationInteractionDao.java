package com.example.fridgewise.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.fridgewise.model.NotificationInteraction;
import java.util.List;

@Dao
public interface NotificationInteractionDao {
    @Insert
    void insert(NotificationInteraction interaction);

    @Query("SELECT * FROM notification_interactions WHERE type = :type AND itemId = :itemId ORDER BY timestamp DESC")
    List<NotificationInteraction> getInteractionsForItem(String type, int itemId);

    @Query("SELECT * FROM notification_interactions WHERE type = :type AND itemId = :itemId AND `action` = 'DISMISSED' ORDER BY timestamp DESC")
    List<NotificationInteraction> getDismissalsForItem(String type, int itemId);
    
    @Query("SELECT COUNT(*) FROM notification_interactions WHERE timestamp > :since")
    int getNotificationCountSince(long since);
}
