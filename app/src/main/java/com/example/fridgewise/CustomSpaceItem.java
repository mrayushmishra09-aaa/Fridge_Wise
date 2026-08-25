package com.example.fridgewise;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(
    tableName = "custom_space_items",
    foreignKeys = @ForeignKey(
        entity = CustomSpace.class,
        parentColumns = "id",
        childColumns = "spaceId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("spaceId")}
)
public class CustomSpaceItem implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int spaceId;
    private String name;
    private double quantity;
    private String unit;
    private String date;
    private String reminderTime;
    private String notes;
    private boolean isChecked; // For Checklist Mode
    private int progressValue; // For Progress Tracking
    private String itemImageUri; // For Document/Gallery

    public CustomSpaceItem(int spaceId, String name, double quantity, String unit, String date, String reminderTime, String notes) {
        this.spaceId = spaceId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.date = date;
        this.reminderTime = reminderTime;
        this.notes = notes;
        this.isChecked = false;
        this.progressValue = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSpaceId() { return spaceId; }
    public void setSpaceId(int spaceId) { this.spaceId = spaceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }

    public int getProgressValue() { return progressValue; }
    public void setProgressValue(int progressValue) { this.progressValue = progressValue; }

    public String getItemImageUri() { return itemImageUri; }
    public void setItemImageUri(String itemImageUri) { this.itemImageUri = itemImageUri; }
}
