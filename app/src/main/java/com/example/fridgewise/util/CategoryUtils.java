package com.example.fridgewise.util;

import com.example.fridgewise.R;

public class CategoryUtils {

    public static int getCategoryIcon(String category) {
        if (category == null) return R.drawable.logo_img;
        
        switch (category.toLowerCase()) {
            case "dairy":
                return R.drawable.dairy_img01;
            case "vegetable":
                return R.drawable.vegi_img01;
            case "fruits":
                return R.drawable.fruits_img01;
            case "non-veg":
                return R.drawable.non_veg_img01;
            case "drinks":
                return R.drawable.drinks_img01;
            case "frozen-food":
                return R.drawable.frozen_img01;
            case "snacks":
                return R.drawable.snacks_img01;
            case "bakery":
                return R.drawable.bakery_img01;
            case "others":
                return R.drawable.grain_rain_flour_img01;
            default:
                return R.drawable.logo_img;
        }
    }

    public static int getPriorityIcon(String priority) {
        if (priority == null) return R.drawable.ic_todo_item;

        switch (priority.toLowerCase()) {
            case "high":
                return R.drawable.high_prio_img;
            case "medium":
                return R.drawable.medium_prio_img;
            case "low":
                return R.drawable.low_prio_img;
            default:
                return R.drawable.ic_todo_item;
        }
    }
    
    public static int getAddItemPlaceholderIcon(String category) {
        if (category == null || category.isEmpty()) return R.drawable.round_camera_alt_24;
        
        int resId = getCategoryIcon(category);
        if (resId == R.drawable.logo_img) {
            return R.drawable.round_camera_alt_24;
        }
        return resId;
    }
}
