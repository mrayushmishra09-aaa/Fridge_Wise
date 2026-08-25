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
    private CustomSpace parentSpace;

    public interface OnItemClickListener {
        void onItemClick(CustomSpaceItem item);
        void onDeleteClick(CustomSpaceItem item);
        void onCheckChanged(CustomSpaceItem item, boolean isChecked);
    }

    public CustomSpaceItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CustomSpaceItem> items, CustomSpace parentSpace) {
        this.items = items;
        this.parentSpace = parentSpace;
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
        
        if (parentSpace != null && parentSpace.isHasTracking()) {
            holder.tvDetails.setVisibility(View.VISIBLE);
            holder.tvDetails.setText(item.getQuantity() + " " + item.getUnit() + " • " + item.getDate());
        } else {
            holder.tvDetails.setVisibility(View.GONE);
        }

        if (parentSpace != null && parentSpace.isHasTodoList()) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setChecked(item.isChecked());
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                listener.onCheckChanged(item, isChecked);
            });
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        if (parentSpace != null && parentSpace.isHasProgressBar()) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressBar.setProgress(item.getProgressValue());
        } else {
            holder.progressBar.setVisibility(View.GONE);
        }

        if (parentSpace != null && parentSpace.isHasDocuments()) {
            holder.cardImage.setVisibility(View.VISIBLE);
            if (item.getItemImageUri() != null) {
                // TODO: Load actual image using Glide/Picasso if needed
                // holder.ivImage.setImageURI(Uri.parse(item.getItemImageUri()));
            }
        } else {
            holder.cardImage.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;
        android.widget.CheckBox checkBox;
        android.widget.ProgressBar progressBar;
        View cardImage;
        android.widget.ImageView ivImage, btnDelete;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvDetails = itemView.findViewById(R.id.tvItemDetails);
            checkBox = itemView.findViewById(R.id.itemCheckBox);
            progressBar = itemView.findViewById(R.id.itemProgressBar);
            cardImage = itemView.findViewById(R.id.itemImageCard);
            ivImage = itemView.findViewById(R.id.ivItemImage);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
