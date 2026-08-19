package com.example.fridgewise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CustomSpaceItemAdapter extends RecyclerView.Adapter<CustomSpaceItemAdapter.ItemViewHolder> {

    private List<CustomSpaceItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CustomSpaceItem item);
        void onDeleteClick(CustomSpaceItem item);
    }

    public CustomSpaceItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CustomSpaceItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_space_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        CustomSpaceItem item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvDetails.setText(item.getQuantity() + " " + item.getUnit() + " • " + item.getDate());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.itemView.findViewById(R.id.btnDelete).setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvDetails = itemView.findViewById(R.id.tvItemDetails);
        }
    }
}
