package com.example.fridgewise.adapter;

import com.example.fridgewise.R;
import com.example.fridgewise.model.CustomSpace;
import com.google.android.material.card.MaterialCardView;

import android.content.res.ColorStateList;
import android.net.Uri;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CustomSpaceAdapter extends RecyclerView.Adapter<CustomSpaceAdapter.SpaceViewHolder> {

    private List<CustomSpace> spaces = new ArrayList<>();
    private SparseIntArray spaceItemCounts = new SparseIntArray();
    private final OnSpaceClickListener listener;

    public interface OnSpaceClickListener {
        void onSpaceClick(CustomSpace space);
        void onSpaceLongClick(CustomSpace space, View view);
    }

    public CustomSpaceAdapter(OnSpaceClickListener listener) {
        this.listener = listener;
    }

    public void setSpaces(List<CustomSpace> spaces, SparseIntArray counts) {
        this.spaces = spaces;
        this.spaceItemCounts = counts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SpaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_space, parent, false);
        return new SpaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpaceViewHolder holder, int position) {
        CustomSpace space = spaces.get(position);
        holder.tvName.setText(space.getName());
        
        int itemCount = spaceItemCounts.get(space.getId(), 0);
        holder.tvCount.setText(itemCount + (itemCount == 1 ? " item" : " items"));
        
        // Theme Polish: Apply space color
        if (space.getColorCode() != 0) {
            int color = space.getColorCode();
            holder.viewColorAccent.setVisibility(View.VISIBLE);
            holder.viewColorAccent.setBackgroundColor(color);
            holder.icon.setImageTintList(ColorStateList.valueOf(color));
            holder.iconContainer.setStrokeColor(ColorStateList.valueOf(color));
            
            // Subtle background tint
            int alphaColor = (color & 0x00FFFFFF) | 0x1A000000; // 10% alpha
            holder.iconContainer.setCardBackgroundColor(alphaColor);
        } else {
            holder.viewColorAccent.setVisibility(View.GONE);
            holder.icon.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.green_primary)));
            holder.iconContainer.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.bg_secondary));
            holder.iconContainer.setStrokeColor(ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.divider_color)));
        }

        if (space.getImageUri() != null && !space.getImageUri().isEmpty()) {
            holder.icon.setImageURI(Uri.parse(space.getImageUri()));
            holder.icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.icon.setImageTintList(null); // Remove tint for custom photos
        } else {
            holder.icon.setImageResource(space.getIconResId());
            holder.icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        holder.itemView.setOnClickListener(v -> listener.onSpaceClick(space));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onSpaceLongClick(space, v);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return spaces.size();
    }

    static class SpaceViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView tvName, tvCount;
        View viewColorAccent;
        MaterialCardView iconContainer;

        public SpaceViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iconSpace);
            tvName = itemView.findViewById(R.id.tvSpaceName);
            tvCount = itemView.findViewById(R.id.tvSpaceItemCount);
            viewColorAccent = itemView.findViewById(R.id.viewColorAccent);
            iconContainer = itemView.findViewById(R.id.iconContainer);
        }
    }
}
