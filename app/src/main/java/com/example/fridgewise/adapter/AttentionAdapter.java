package com.example.fridgewise.adapter;

import com.example.fridgewise.R;
import com.example.fridgewise.model.AttentionItem;
import com.example.fridgewise.util.ProgressBorderDrawable;
import com.example.fridgewise.util.TimeProgressUtils;

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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

public class AttentionAdapter extends ListAdapter<AttentionItem, AttentionAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(AttentionItem item);
    }

    private Context context;
    private OnItemClickListener listener;

    public AttentionAdapter(Context context, OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<AttentionItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull AttentionItem oldItem, @NonNull AttentionItem newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull AttentionItem oldItem, @NonNull AttentionItem newItem) {
                return oldItem.getName().equals(newItem.getName()) &&
                       oldItem.getBadgeText().equals(newItem.getBadgeText()) &&
                       oldItem.getHint().equals(newItem.getHint()) &&
                       oldItem.getTargetTimestamp() == newItem.getTargetTimestamp() &&
                       oldItem.getCreationTimestamp() == newItem.getCreationTimestamp();
            }
        });
        this.context = context;
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
        AttentionItem item = getItem(position);
        holder.tvName.setText(item.getName());
        
        if (item.getBadgeText() != null && !item.getBadgeText().isEmpty()) {
            holder.tvBadge.setText(item.getBadgeText());
            holder.tvBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvBadge.setVisibility(View.GONE);
        }
        
        holder.tvLocation.setText(item.getLocation());
        holder.tvHint.setText(item.getHint());
        holder.tvAction.setText(item.getActionText());

        // Apply Colors
        int redColor = ContextCompat.getColor(context, R.color.badge_red_text);

        if (item.getBadgeTextColor() == redColor) {
            holder.tvBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_attention_badge_red));
        }

        if (item.getBadgeTextColor() != 0) {
            holder.tvBadge.setTextColor(item.getBadgeTextColor());
        }
        
        if (item.getStatusColor() != 0) {
            holder.ivStatusDot.setImageTintList(ColorStateList.valueOf(item.getStatusColor()));
        }

        // Image
        if (item.getImageResId() != 0) {
            holder.ivImage.setImageResource(item.getImageResId());
            // Tint vector icons for dark mode visibility
            if (item.getImageResId() == R.drawable.ic_todo_item) {
                holder.ivImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_primary)));
            } else {
                holder.ivImage.setImageTintList(null);
            }
        } else {
            holder.ivImage.setImageResource(R.drawable.logo_img);
            holder.ivImage.setImageTintList(null);
        }

        // Dynamic Segmented Progressive Card Border Outline (3.5dp width, 3 segments: 0-50% Green, 50-85% Orange, 85-100% Red)
        TimeProgressUtils.ProgressState progressState = TimeProgressUtils.calculateState(item.getTargetTimestamp(), item.getCreationTimestamp());
        
        // Dynamically set Badge background and text color according to time progress urgency
        if (progressState.getProgressPercent() < 50) {
            holder.tvBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_chip_green));
            holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.green_primary));
        } else if (progressState.getProgressPercent() < 85) {
            holder.tvBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_attention_badge_orange));
            holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.attention_badge_orange_text));
        } else {
            holder.tvBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_attention_badge_red));
            holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_red_text));
        }

        if (holder.cardView != null) {
            float percent = progressState.isVisible() ? (progressState.getProgressPercent() / 100f) : 0f;
            float density = context.getResources().getDisplayMetrics().density;
            float cornerRadiusPx = 20f * density;
            float strokeWidthPx = 2.8f * density;
            int trackColor = ContextCompat.getColor(context, R.color.divider_color);
            int green = ContextCompat.getColor(context, R.color.green_primary);
            int orange = ContextCompat.getColor(context, R.color.orange_warning);
            int red = ContextCompat.getColor(context, R.color.red_expired);

            ProgressBorderDrawable borderDrawable = new ProgressBorderDrawable(
                cornerRadiusPx, strokeWidthPx, density, trackColor, green, orange, red
            );
            borderDrawable.setProgress(percent);
            holder.cardView.setForeground(borderDrawable);
            holder.cardView.setStrokeWidth(0);
        }

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBadge, tvLocation, tvHint, tvAction;
        ImageView ivImage, ivStatusDot, ivActionChevron;
        LinearLayout btnAction;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
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
