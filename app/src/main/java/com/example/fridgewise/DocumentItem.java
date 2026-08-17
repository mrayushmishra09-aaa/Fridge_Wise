package com.example.fridgewise;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * This class represents a single document in your database.
 */
@Entity(tableName = "documents")
public class DocumentItem implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String category;
    private String imagePath;

    public DocumentItem(String name, String category, String imagePath) {
        this.name = name;
        this.category = category;
        this.imagePath = imagePath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
