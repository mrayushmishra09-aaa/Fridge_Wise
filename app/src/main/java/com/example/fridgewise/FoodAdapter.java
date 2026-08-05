package com.example.fridgewise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
        holder.tvExpiry.setText("Expires: " + currentItem.getExpiryDate());
        holder.tvQuantity.setText(currentItem.getQuantity() + " " + currentItem.getUnit());

        // Correctly set Edit listener
        holder.editIcon.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(currentItem);
            }
        });

        // Correctly set Delete listener
        holder.deleteIcon.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(currentItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvExpiry, tvQuantity;
        ImageView editIcon, deleteIcon;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.itemName);
            tvCategory = itemView.findViewById(R.id.itemCategory);
            tvExpiry = itemView.findViewById(R.id.itemExpiry);
            tvQuantity = itemView.findViewById(R.id.itemQuantity);
            editIcon = itemView.findViewById(R.id.editicon);
            deleteIcon = itemView.findViewById(R.id.deleteicon);
        }
    }
}
