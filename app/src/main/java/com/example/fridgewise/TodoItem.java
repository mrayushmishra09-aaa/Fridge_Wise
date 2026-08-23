package com.example.fridgewise;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * Entity class representing a To-Do item in the Room database.
 */
@Entity(tableName = "todo_items")
public class TodoItem implements Serializable {


    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String date;
    private String time;
    private String priority;
    private String note;
    private boolean isReminderSet;
    private boolean isCompleted;

    /**
     * Default constructor required by Room.
     */
    public TodoItem() {
    }

    /**
     * Constructor used to create a new To-Do item.
     */
    public TodoItem(String title, String date, String time, String priority, String note, boolean isReminderSet, boolean isCompleted) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.priority = priority;
        this.note = note;
        this.isReminderSet = isReminderSet;
        this.isCompleted = isCompleted;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isReminderSet() { return isReminderSet; }
    public void setReminderSet(boolean reminderSet) { isReminderSet = reminderSet; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
