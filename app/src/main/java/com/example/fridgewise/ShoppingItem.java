package com.example.fridgewise;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "shopping_items")
public class ShoppingItem implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @NonNull
    private String name = "";
    
    private String quantity;
    private String unit;
    private boolean isCompleted;

    public ShoppingItem() {
    }

    public ShoppingItem(@NonNull String name, String quantity, String unit, boolean isCompleted) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.isCompleted = isCompleted;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
