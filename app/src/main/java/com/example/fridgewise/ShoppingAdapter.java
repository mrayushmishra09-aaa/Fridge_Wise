package com.example.fridgewise;

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
import java.util.List;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ShoppingViewHolder> {

    private List<ShoppingItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(ShoppingItem item);
        void onDeleteClick(ShoppingItem item);
        void onStatusChange(ShoppingItem item, boolean isCompleted);
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

    static class ShoppingViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity;
        CheckBox cbCompleted;
        ImageButton btnMore;

        public ShoppingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvItemQuantity);
            cbCompleted = itemView.findViewById(R.id.cbShoppingItem);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
