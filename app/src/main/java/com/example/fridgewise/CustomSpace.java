package com.example.fridgewise;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "custom_spaces")
public class CustomSpace implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private int iconResId; // Default icon resource
    private String imageUri; // Optional custom photo URI

    public CustomSpace(String name, int iconResId, String imageUri) {
        this.name = name;
        this.iconResId = iconResId;
        this.imageUri = imageUri;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}
