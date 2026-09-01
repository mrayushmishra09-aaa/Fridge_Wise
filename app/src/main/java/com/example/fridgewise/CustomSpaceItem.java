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
    private Long reminderTimestamp;
    private String notes;
    private boolean isChecked; // For Checklist Mode
    private String documentUri; // For PDF/Docs
    private String documentName; // Display name of file
    private Long completionTimestamp; // Time when marked completed

    public CustomSpaceItem(int spaceId, String name, double quantity, String unit, String date, Long reminderTimestamp, String notes) {
        this.spaceId = spaceId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.date = date;
        this.reminderTimestamp = reminderTimestamp;
        this.notes = notes;
        this.isChecked = false;
        this.completionTimestamp = null;
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

    public Long getReminderTimestamp() { return reminderTimestamp; }
    public void setReminderTimestamp(Long reminderTimestamp) { this.reminderTimestamp = reminderTimestamp; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }

    public String getDocumentUri() { return documentUri; }
    public void setDocumentUri(String documentUri) { this.documentUri = documentUri; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public Long getCompletionTimestamp() { return completionTimestamp; }
    public void setCompletionTimestamp(Long completionTimestamp) { this.completionTimestamp = completionTimestamp; }
}
