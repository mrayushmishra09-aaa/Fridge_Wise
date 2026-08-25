package com.example.fridgewise;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "custom_spaces")
public class CustomSpace implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String description;
    private int iconResId; // Default icon resource
    private int colorCode; // Theme color code
    private String privacyStatus; // e.g. "Private", "Shared"
    private String imageUri; // Optional custom photo URI

    // Advanced features
    private boolean hasProgressBar;
    private boolean hasTodoList;
    private boolean hasDocuments;
    private boolean hasTracking;

    public CustomSpace(String name, int iconResId, String imageUri) {
        this.name = name;
        this.iconResId = iconResId;
        this.imageUri = imageUri;
        this.description = "";
        this.colorCode = 0;
        this.privacyStatus = "Private";
        this.hasProgressBar = false;
        this.hasTodoList = false;
        this.hasDocuments = false;
        this.hasTracking = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }

    public int getColorCode() { return colorCode; }
    public void setColorCode(int colorCode) { this.colorCode = colorCode; }

    public String getPrivacyStatus() { return privacyStatus; }
    public void setPrivacyStatus(String privacyStatus) { this.privacyStatus = privacyStatus; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public boolean isHasProgressBar() { return hasProgressBar; }
    public void setHasProgressBar(boolean hasProgressBar) { this.hasProgressBar = hasProgressBar; }

    public boolean isHasTodoList() { return hasTodoList; }
    public void setHasTodoList(boolean hasTodoList) { this.hasTodoList = hasTodoList; }

    public boolean isHasDocuments() { return hasDocuments; }
    public void setHasDocuments(boolean hasDocuments) { this.hasDocuments = hasDocuments; }

    public boolean isHasTracking() { return hasTracking; }
    public void setHasTracking(boolean hasTracking) { this.hasTracking = hasTracking; }
}
