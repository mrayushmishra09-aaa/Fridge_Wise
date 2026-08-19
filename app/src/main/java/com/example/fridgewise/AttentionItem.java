package com.example.fridgewise;

public class AttentionItem {
    public enum Type { FOOD, MEDICINE, TODO }
    
    private String id;
    private String name;
    private String badgeText;
    private String location;
    private String hint;
    private String actionText;
    private String imageUri;
    private int imageResId;
    private Type type;
    private int statusColor;
    private int badgeBgColor;
    private int badgeTextColor;
    private long priorityScore;

    public AttentionItem(String id, String name, String badgeText, String location, String hint, String actionText, Type type) {
        this.id = id;
        this.name = name;
        this.badgeText = badgeText;
        this.location = location;
        this.hint = hint;
        this.actionText = actionText;
        this.type = type;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getBadgeText() { return badgeText; }
    public String getLocation() { return location; }
    public String getHint() { return hint; }
    public String getActionText() { return actionText; }
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public Type getType() { return type; }
    public int getStatusColor() { return statusColor; }
    public void setStatusColor(int statusColor) { this.statusColor = statusColor; }
    public int getBadgeBgColor() { return badgeBgColor; }
    public void setBadgeBgColor(int badgeBgColor) { this.badgeBgColor = badgeBgColor; }
    public int getBadgeTextColor() { return badgeTextColor; }
    public void setBadgeTextColor(int badgeTextColor) { this.badgeTextColor = badgeTextColor; }
    public long getPriorityScore() { return priorityScore; }
    public void setPriorityScore(long priorityScore) { this.priorityScore = priorityScore; }
}
