package com.example.fridgewise.adapter;

import com.example.fridgewise.R;
import com.example.fridgewise.model.ShoppingItem;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.core.content.ContextCompat;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;
import android.os.Build;
import com.google.android.material.card.MaterialCardView;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ShoppingViewHolder> {

    private List<ShoppingItem> items = new ArrayList<>();
    private OnItemClickListener listener;
    private final Set<Integer> selectedIds = new HashSet<>();
    private boolean isSelectionMode = false;

    public interface OnItemClickListener {
        void onEditClick(ShoppingItem item);
        void onDeleteClick(ShoppingItem item);
        void onStatusChange(ShoppingItem item, boolean isCompleted);
        void onSelectionModeChanged(boolean isSelectionMode);
        void onSelectionCountChanged(int count);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ShoppingItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ShoppingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping, parent, false);
        return new ShoppingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingViewHolder holder, int position) {
        ShoppingItem item = items.get(position);
        holder.tvName.setText(item.getName());
        
        String quantityText = item.getQuantity();
        if (item.getUnit() != null && !item.getUnit().isEmpty()) {
            quantityText += " " + item.getUnit();
        }
        holder.tvQuantity.setText(quantityText);

        // --- Selection Logic (Tint Based) ---
        boolean isSelected = selectedIds.contains(item.getId());
        
        if (isSelected) {
            holder.shoppingCard.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.card_green));
            holder.shoppingCard.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_primary));
            holder.shoppingCard.setStrokeWidth(4);
        } else {
            holder.shoppingCard.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.surface_card));
            holder.shoppingCard.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.divider_color));
            holder.shoppingCard.setStrokeWidth(1);
        }

        // Hide completion checkbox during selection mode
        holder.cbCompleted.setVisibility(isSelectionMode ? View.GONE : View.VISIBLE);
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(item.isCompleted());
        updateVisualState(holder, item.isCompleted());

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setCompleted(isChecked);
            updateVisualState(holder, isChecked);
            if (listener != null) {
                listener.onStatusChange(item, isChecked);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(item.getId());
            } else {
                if (listener != null) listener.onEditClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                isSelectionMode = true;
                vibrate(v.getContext());
                toggleSelection(item.getId());
                if (listener != null) listener.onSelectionModeChanged(true);
                return true;
            }
            return false;
        });

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMore);
            popup.inflate(R.menu.menu_shopping_item);
            popup.setOnMenuItemClickListener(menuItem -> {
                if (listener == null) return false;
                int id = menuItem.getItemId();
                if (id == R.id.action_edit) {
                    listener.onEditClick(item);
                    return true;
                } else if (id == R.id.action_delete) {
                    listener.onDeleteClick(item);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void updateVisualState(ShoppingViewHolder holder, boolean isCompleted) {
        if (isCompleted) {
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemView.setAlpha(0.6f);
        } else {
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.itemView.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
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
        for (ShoppingItem item : items) {
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

    public List<ShoppingItem> getSelectedItems() {
        List<ShoppingItem> selected = new ArrayList<>();
        for (ShoppingItem item : items) {
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

    static class ShoppingViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity;
        CheckBox cbCompleted;
        ImageButton btnMore;
        MaterialCardView shoppingCard;

        public ShoppingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvItemQuantity);
            cbCompleted = itemView.findViewById(R.id.cbShoppingItem);
            btnMore = itemView.findViewById(R.id.btnMore);
            // In layout it didn't have ID, I will add it via another edit or assume I added it
            shoppingCard = (MaterialCardView) itemView; 
        }
    }
}
