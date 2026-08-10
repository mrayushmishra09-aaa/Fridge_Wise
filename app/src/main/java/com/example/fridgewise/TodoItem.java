package com.example.fridgewise;

public class TodoItem {
    private String title;
    private String time;
    private String priority;
    private boolean isCompleted;

    public TodoItem(String title, String time, String priority, boolean isCompleted) {
        this.title = title;
        this.time = time;
        this.priority = priority;
        this.isCompleted = isCompleted;
    }

    public String getTitle() { return title; }
    public String getTime() { return time; }
    public String getPriority() { return priority; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
