package com.example.fridgewise;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "activity_records")
public class ActivityRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String type; // FOOD, MEDICINE, TODO, CUSTOM
    private String action; // Added, Updated, Deleted
    private String itemName;
    private long timestamp;
    private int iconRes;

    public ActivityRecord(String type, String action, String itemName, long timestamp, int iconRes) {
        this.type = type;
        this.action = action;
        this.itemName = itemName;
        this.timestamp = timestamp;
        this.iconRes = iconRes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getIconRes() { return iconRes; }
    public void setIconRes(int iconRes) { this.iconRes = iconRes; }
}
