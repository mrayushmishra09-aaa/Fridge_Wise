package com.example.fridgewise;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "food_items")
public class FoodItem implements Serializable {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @NonNull
    private String name = "";
    
    private double quantity;
    private String unit; 
    private String category;
    private String purchaseDate;
    private String expiryDate;
    private long expiryTimestamp;
    private String imageUri;
    private String notes;

    public FoodItem() {
    }

    public FoodItem(@NonNull String name, double quantity, String unit, String category, String purchaseDate, String expiryDate) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public long getExpiryTimestamp() { return expiryTimestamp; }
    public void setExpiryTimestamp(long expiryTimestamp) { this.expiryTimestamp = expiryTimestamp; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}
