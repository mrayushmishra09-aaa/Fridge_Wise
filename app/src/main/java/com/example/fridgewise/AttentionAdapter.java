package com.example.fridgewise;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AttentionAdapter extends RecyclerView.Adapter<AttentionAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(AttentionItem item);
    }

    private List<AttentionItem> items;
    private Context context;
    private OnItemClickListener listener;

    public AttentionAdapter(Context context, List<AttentionItem> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attention, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttentionItem item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvBadge.setText(item.getBadgeText());
        holder.tvLocation.setText(item.getLocation());
        holder.tvHint.setText(item.getHint());
        holder.tvAction.setText(item.getActionText());

        // Apply Colors
        holder.tvBadge.setBackgroundTintList(ColorStateList.valueOf(item.getBadgeBgColor()));
        holder.tvBadge.setTextColor(item.getBadgeTextColor());
        holder.ivStatusDot.setImageTintList(ColorStateList.valueOf(item.getStatusColor()));
        holder.btnAction.setBackgroundTintList(ColorStateList.valueOf(item.getBadgeBgColor()));
        holder.tvAction.setTextColor(item.getBadgeTextColor());
        holder.ivActionChevron.setImageTintList(ColorStateList.valueOf(item.getBadgeTextColor()));

        // Image
        if (item.getImageResId() != 0) {
            holder.ivImage.setImageResource(item.getImageResId());
        } else {
            // Placeholder or Glide implementation
            holder.ivImage.setImageResource(R.drawable.logo_img);
        }

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBadge, tvLocation, tvHint, tvAction;
        ImageView ivImage, ivStatusDot, ivActionChevron;
        LinearLayout btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvBadge = itemView.findViewById(R.id.tv_badge);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvHint = itemView.findViewById(R.id.tv_hint);
            tvAction = itemView.findViewById(R.id.tv_action_text);
            ivImage = itemView.findViewById(R.id.iv_item_image);
            ivStatusDot = itemView.findViewById(R.id.iv_status_dot);
            ivActionChevron = itemView.findViewById(R.id.iv_action_chevron);
            btnAction = itemView.findViewById(R.id.btn_view_action);
        }
    }
}