package com.example.fridgewise;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {

    private List<ActivityRecord> activities = new ArrayList<>();
    private final Context context;

    public RecentActivityAdapter(Context context) {
        this.context = context;
    }

    public void setActivities(List<ActivityRecord> activities) {
        this.activities = activities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityRecord activity = activities.get(position);
        
        String actionText = activity.getAction() + " " + activity.getItemName();
        holder.tvTitle.setText(actionText);
        holder.tvType.setText(activity.getType());
        
        String timeAgo = DateUtils.getRelativeTimeSpanString(activity.getTimestamp(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
        holder.tvTime.setText(timeAgo);
        
        if (activity.getIconRes() != 0) {
            holder.ivIcon.setImageResource(activity.getIconRes());
        }
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvType, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_activity_icon);
            tvTitle = itemView.findViewById(R.id.tv_activity_title);
            tvType = itemView.findViewById(R.id.tv_activity_type);
            tvTime = itemView.findViewById(R.id.tv_activity_time);
        }
    }
}
