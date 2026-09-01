package com.example.fridgewise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class RecipeDetailsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_RECIPE = "arg_recipe";
    private RecipeItem recipe;

    public static RecipeDetailsBottomSheet newInstance(RecipeItem recipe) {
        RecipeDetailsBottomSheet fragment = new RecipeDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RECIPE, recipe);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recipe = (RecipeItem) getArguments().getSerializable(ARG_RECIPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_recipe_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvName = view.findViewById(R.id.tvDetailName);
        TextView tvTime = view.findViewById(R.id.tvDetailTime);
        TextView tvMatch = view.findViewById(R.id.tvDetailMatch);
        TextView tvIngredients = view.findViewById(R.id.tvDetailIngredients);
        TextView tvInstructions = view.findViewById(R.id.tvDetailInstructions);
        View btnCookNow = view.findViewById(R.id.btnCookNow);

        if (recipe != null) {
            tvName.setText(recipe.getName());
            tvTime.setText(recipe.getTime());
            tvMatch.setText(recipe.getMatchPercentage() + "% Match");
            
            // Format ingredients and instructions if they are not already bulleted/numbered
            tvIngredients.setText(formatList(recipe.getIngredients(), "• "));
            tvInstructions.setText(formatList(recipe.getInstructions(), ""));
        }

        btnCookNow.setOnClickListener(v -> {
            // Future feature: Mark items as consumed
            dismiss();
        });
    }

    private String formatList(String text, String prefix) {
        if (text == null || text.isEmpty()) return "";
        
        // If it already looks formatted, just return it
        if (text.contains("•") || text.contains("1.")) return text;
        
        StringBuilder formatted = new StringBuilder();
        String[] items = text.split("[,\\n]");
        for (int i = 0; i < items.length; i++) {
            String item = items[i].trim();
            if (!item.isEmpty()) {
                if (prefix.isEmpty()) {
                    formatted.append(i + 1).append(". ").append(item).append("\n");
                } else {
                    formatted.append(prefix).append(item).append("\n");
                }
            }
        }
        return formatted.toString().trim();
    }
}
