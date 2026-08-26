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

    // Capabilities (Item Features)
    private boolean hasCheckbox;
    private boolean hasReminder;
    private boolean hasNotes;
    private boolean hasQuantity;
    private boolean hasDate;
    private boolean hasImage;
    private boolean hasAttachments;

    // Completion behavior
    private int autoRemoveDuration; // 0: Never, 1: 24h, 7: 7 days

    public CustomSpace(String name, int iconResId, String imageUri) {
        this.name = name;
        this.iconResId = iconResId;
        this.imageUri = imageUri;
        this.description = "";
        this.colorCode = 0;
        this.privacyStatus = "Private";
        this.hasCheckbox = false;
        this.hasReminder = false;
        this.hasNotes = false;
        this.hasQuantity = false;
        this.hasDate = false;
        this.hasImage = false;
        this.hasAttachments = false;
        this.autoRemoveDuration = 0;
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

    public boolean isHasCheckbox() { return hasCheckbox; }
    public void setHasCheckbox(boolean hasCheckbox) { this.hasCheckbox = hasCheckbox; }

    public boolean isHasReminder() { return hasReminder; }
    public void setHasReminder(boolean hasReminder) { this.hasReminder = hasReminder; }

    public boolean isHasNotes() { return hasNotes; }
    public void setHasNotes(boolean hasNotes) { this.hasNotes = hasNotes; }

    public boolean isHasQuantity() { return hasQuantity; }
    public void setHasQuantity(boolean hasQuantity) { this.hasQuantity = hasQuantity; }

    public boolean isHasDate() { return hasDate; }
    public void setHasDate(boolean hasDate) { this.hasDate = hasDate; }

    public boolean isHasImage() { return hasImage; }
    public void setHasImage(boolean hasImage) { this.hasImage = hasImage; }

    public boolean isHasAttachments() { return hasAttachments; }
    public void setHasAttachments(boolean hasAttachments) { this.hasAttachments = hasAttachments; }

    public int getAutoRemoveDuration() { return autoRemoveDuration; }
    public void setAutoRemoveDuration(int autoRemoveDuration) { this.autoRemoveDuration = autoRemoveDuration; }
}
