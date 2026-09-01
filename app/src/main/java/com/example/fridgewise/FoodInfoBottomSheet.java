package com.example.fridgewise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FoodInfoBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_FOOD = "arg_food";
    private FoodItem foodItem;

    public static FoodInfoBottomSheet newInstance(FoodItem foodItem) {
        FoodInfoBottomSheet fragment = new FoodInfoBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FOOD, foodItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.FloatingBottomSheetDialog);
        if (getArguments() != null) {
            foodItem = (FoodItem) getArguments().getSerializable(ARG_FOOD);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_food_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvName = view.findViewById(R.id.tvInfoName);
        TextView tvCategory = view.findViewById(R.id.tvInfoCategory);
        TextView tvQuantity = view.findViewById(R.id.tvInfoQuantity);
        TextView tvExpiry = view.findViewById(R.id.tvInfoExpiry);
        TextView tvPurchase = view.findViewById(R.id.tvInfoPurchase);
        TextView tvNotes = view.findViewById(R.id.tvInfoNotes);
        View btnClose = view.findViewById(R.id.btnInfoClose);

        if (foodItem != null) {
            tvName.setText(foodItem.getName());
            tvCategory.setText(foodItem.getCategory());
            tvQuantity.setText(foodItem.getQuantity() + " " + foodItem.getUnit());
            tvExpiry.setText(foodItem.getExpiryDate());
            tvPurchase.setText(foodItem.getPurchaseDate());
            
            String notes = foodItem.getNotes();
            if (notes != null && !notes.isEmpty()) {
                tvNotes.setText(notes);
            } else {
                tvNotes.setText("No additional notes for this item.");
            }
        }

        btnClose.setOnClickListener(v -> dismiss());
    }
}
