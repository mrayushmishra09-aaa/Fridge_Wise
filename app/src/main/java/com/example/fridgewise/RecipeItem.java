package com.example.fridgewise;

import java.io.Serializable;

public class RecipeItem implements Serializable {
    private String name;
    private String time;
    private int matchPercentage;
    private int imageResId;
    private String instructions;
    private String ingredients;
    private int extraItemsCount;

    public RecipeItem(String name, String time, int matchPercentage, int imageResId) {
        this.name = name;
        this.time = time;
        this.matchPercentage = matchPercentage;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public String getTime() { return time; }
    public int getMatchPercentage() { return matchPercentage; }
    public int getImageResId() { return imageResId; }
    
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public int getExtraItemsCount() { return extraItemsCount; }
    public void setExtraItemsCount(int count) { this.extraItemsCount = count; }
}
