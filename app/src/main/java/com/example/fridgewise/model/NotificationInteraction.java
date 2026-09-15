package com.example.fridgewise.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notification_interactions")
public class NotificationInteraction {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String type; // MEDICINE, TODO, FOOD
    private int itemId;
    private String action; // DELIVERED, CLICKED, DISMISSED, SNOOZED
    private long timestamp;

    public NotificationInteraction(String type, int itemId, String action, long timestamp) {
        this.type = type;
        this.itemId = itemId;
        this.action = action;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public int getItemId() { return itemId; }
    public String getAction() { return action; }
    public long getTimestamp() { return timestamp; }
}
