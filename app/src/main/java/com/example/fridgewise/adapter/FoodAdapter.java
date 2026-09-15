package com.example.fridgewise.adapter;

import com.example.fridgewise.R;
import com.example.fridgewise.model.FoodItem;
import com.example.fridgewise.util.CategoryUtils;

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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.core.content.ContextCompat;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;
import android.os.Build;
import com.google.android.material.card.MaterialCardView;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public interface onItemClickListener {
        void onEditClick(FoodItem foodItem);
        void onDeleteClick(FoodItem foodItem);
        void onInfoClick(FoodItem foodItem);
        void onSelectionModeChanged(boolean isSelectionMode);
        void onSelectionCountChanged(int count);
    }

    private List<FoodItem> foodList = new ArrayList<>();
    private onItemClickListener listener;
    private final Set<Integer> selectedIds = new HashSet<>();
    private boolean isSelectionMode = false;

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
        
        String formattedQty = formatQuantity(currentItem.getQuantity());
        holder.tvQuantity.setText(formattedQty + " " + currentItem.getUnit());

        // Category-based image linking
        setCategoryImage(holder.imgItem, currentItem.getCategory());

        // Show/hide barcode verified icon
        if (currentItem.getBarcode() != null && !currentItem.getBarcode().isEmpty()) {
            holder.icBarcodeVerified.setVisibility(View.VISIBLE);
        } else {
            holder.icBarcodeVerified.setVisibility(View.GONE);
        }

        // --- Selection Logic (Tint Based) ---
        boolean isSelected = selectedIds.contains(currentItem.getId());
        
        if (isSelected) {
            holder.foodCard.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.card_green));
            holder.foodCard.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_primary));
            holder.foodCard.setStrokeWidth(4);
        } else {
            holder.foodCard.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.surface_card));
            holder.foodCard.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.divider_color));
            holder.foodCard.setStrokeWidth(1);
        }

        holder.btnInfo.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(currentItem.getId());
            } else {
                if (listener != null) listener.onInfoClick(currentItem);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(currentItem.getId());
            } else {
                if (listener != null) listener.onEditClick(currentItem);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                isSelectionMode = true;
                vibrate(v.getContext());
                toggleSelection(currentItem.getId());
                if (listener != null) listener.onSelectionModeChanged(true);
                return true;
            }
            return false;
        });

        // Three-dot menu logic
        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Edit");
            popup.getMenu().add("Delete");
            
            popup.setOnMenuItemClickListener(item -> {
                CharSequence title = item.getTitle();
                if (title != null) {
                    if (title.equals("Edit")) {
                        if (listener != null) listener.onEditClick(currentItem);
                    } else if (title.equals("Delete")) {
                        if (listener != null) listener.onDeleteClick(currentItem);
                    }
                }
                return true;
            });
            popup.show();
        });
    }

    private void setCategoryImage(ImageView imageView, String category) {
        imageView.setImageResource(CategoryUtils.getCategoryIcon(category));
    }

    private String formatQuantity(double quantity) {
        if (quantity == (long) quantity) {
            return String.format(Locale.getDefault(), "%d", (long) quantity);
        } else {
            return String.format(Locale.getDefault(), "%s", quantity);
        }
    }

    private void toggleSelection(int id) {
        if (selectedIds.contains(id)) selectedIds.remove(id);
        else selectedIds.add(id);
        
        if (selectedIds.isEmpty()) {
            isSelectionMode = false;
            if (listener != null) listener.onSelectionModeChanged(false);
        }
        
        if (listener != null) listener.onSelectionCountChanged(selectedIds.size());
        notifyDataSetChanged();
    }

    public void selectAll() {
        for (FoodItem item : foodList) {
            selectedIds.add(item.getId());
        }
        isSelectionMode = true;
        if (listener != null) {
            listener.onSelectionCountChanged(selectedIds.size());
            listener.onSelectionModeChanged(true);
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedIds.clear();
        isSelectionMode = false;
        if (listener != null) listener.onSelectionCountChanged(0);
        notifyDataSetChanged();
    }

    public List<FoodItem> getSelectedItems() {
        List<FoodItem> selected = new ArrayList<>();
        for (FoodItem item : foodList) {
            if (selectedIds.contains(item.getId())) selected.add(item);
        }
        return selected;
    }

    private void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            else vibrator.vibrate(50);
        }
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvExpiry, tvQuantity;
        ImageView imgItem, btnMore, btnInfo, icBarcodeVerified;
        MaterialCardView foodCard;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.itemName);
            tvCategory = itemView.findViewById(R.id.itemCategory);
            tvExpiry = itemView.findViewById(R.id.itemExpiry);
            tvQuantity = itemView.findViewById(R.id.itemQuantity);
            imgItem = itemView.findViewById(R.id.itemImage);
            btnMore = itemView.findViewById(R.id.btnMore);
            btnInfo = itemView.findViewById(R.id.btnInfo);
            icBarcodeVerified = itemView.findViewById(R.id.ic_barcode_verified);
            foodCard = itemView.findViewById(R.id.foodCard);
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
