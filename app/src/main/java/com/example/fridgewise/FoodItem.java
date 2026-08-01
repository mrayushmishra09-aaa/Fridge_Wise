package com.example.fridgewise;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_items")
public class FoodItem {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @NonNull
    private String name = ""; // Initialized to avoid null warnings
    
    private int quantity;
    private String category;
    private String purchaseDate;
    private String expiryDate;
    private String imageUri;

    // Default constructor required by Room
    public FoodItem() {
    }

    // Constructor for creating new items
    public FoodItem(@NonNull String name, int quantity, String category, String purchaseDate, String expiryDate) {
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
