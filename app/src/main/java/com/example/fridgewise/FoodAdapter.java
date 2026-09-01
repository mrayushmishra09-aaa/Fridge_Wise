package com.example.fridgewise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.PopupMenu;
import java.util.ArrayList;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public interface onItemClickListener {
        void onEditClick(FoodItem foodItem);
        void onDeleteClick(FoodItem foodItem);
        void onInfoClick(FoodItem foodItem);
    }

    private List<FoodItem> foodList = new ArrayList<>();
    private onItemClickListener listener;

    public FoodAdapter(onItemClickListener listener){
        this.listener = listener;
    }

    public void setFoodList(List<FoodItem> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FoodDiffCallback(this.foodList, newList));
        this.foodList.clear();
        this.foodList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public List<FoodItem> getFoodList() {
        return foodList;
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

        holder.btnInfo.setOnClickListener(v -> {
            if (listener != null) listener.onInfoClick(currentItem);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(currentItem);
        });

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
        imageView.setImageResource(CategoryUtils.getCategoryIcon(category));
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvExpiry, tvQuantity;
        ImageView imgItem, btnMore, btnInfo;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.itemName);
            tvCategory = itemView.findViewById(R.id.itemCategory);
            tvExpiry = itemView.findViewById(R.id.itemExpiry);
            tvQuantity = itemView.findViewById(R.id.itemQuantity);
            imgItem = itemView.findViewById(R.id.itemImage);
            btnMore = itemView.findViewById(R.id.btnMore);
            btnInfo = itemView.findViewById(R.id.btnInfo);
        }
    }

    private static class FoodDiffCallback extends DiffUtil.Callback {
        private final List<FoodItem> oldList;
        private final List<FoodItem> newList;

        public FoodDiffCallback(List<FoodItem> oldList, List<FoodItem> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            FoodItem oldItem = oldList.get(oldItemPosition);
            FoodItem newItem = newList.get(newItemPosition);
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getCategory().equals(newItem.getCategory()) &&
                   oldItem.getQuantity() == newItem.getQuantity() &&
                   oldItem.getUnit().equals(newItem.getUnit()) &&
                   oldItem.getExpiryDate().equals(newItem.getExpiryDate());
        }
    }
}
