package com.example.fridgewise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.PopupMenu;
import java.util.ArrayList;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public interface onItemClickListener {
        void onEditClick(FoodItem foodItem);
        void onDeleteClick(FoodItem foodItem);
    }

    private List<FoodItem> foodList = new ArrayList<>();
    private onItemClickListener listener;

    public FoodAdapter(onItemClickListener listener){
        this.listener = listener;
    }

    public void setFoodList(List<FoodItem> foodList) {
        this.foodList = foodList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem currentItem = foodList.get(position);
        holder.tvName.setText(currentItem.getName());
        holder.tvCategory.setText(currentItem.getCategory());
        holder.tvExpiry.setText("Expires " + currentItem.getExpiryDate());
        holder.tvQuantity.setText(currentItem.getQuantity() + " " + currentItem.getUnit());

        // Category-based image linking
        setCategoryImage(holder.imgItem, currentItem.getCategory());

        // Three-dot menu logic
        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Edit");
            popup.getMenu().add("Delete");
            
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Edit")) {
                    if (listener != null) listener.onEditClick(currentItem);
                } else if (item.getTitle().equals("Delete")) {
                    if (listener != null) listener.onDeleteClick(currentItem);
                }
                return true;
            });
            popup.show();
        });
    }

    private void setCategoryImage(ImageView imageView, String category) {
        if (category == null) return;
        
        int resId;
        switch (category.toLowerCase()) {
            case "dairy":
                resId = R.drawable.dairy_img01;
                break;
            case "vegetable":
                resId = R.drawable.vegi_img01;
                break;
            case "fruits":
                resId = R.drawable.fruits_img01;
                break;
            case "non-veg":
                resId = R.drawable.non_veg_img01;
                break;
            case "drinks":
                resId = R.drawable.drinks_img01;
                break;
            case "frozen-food":
                resId = R.drawable.frozen_img01;
                break;
            case "snacks":
                resId = R.drawable.snacks_img01;
                break;
            case "bakery":
                resId = R.drawable.bakery_img01;
                break;
            case "others":
                resId = R.drawable.grain_rain_flour_img01;
                break;
            default:
                resId = R.drawable.logo_img;
                break;
        }
        imageView.setImageResource(resId);
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvExpiry, tvQuantity;
        ImageView imgItem, btnMore;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.itemName);
            tvCategory = itemView.findViewById(R.id.itemCategory);
            tvExpiry = itemView.findViewById(R.id.itemExpiry);
            tvQuantity = itemView.findViewById(R.id.itemQuantity);
            imgItem = itemView.findViewById(R.id.itemImage);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
