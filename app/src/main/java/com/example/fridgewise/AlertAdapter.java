package com.example.fridgewise;

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

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    private List<AlertItem> alertList = new ArrayList<>();

    public void setAlerts(List<AlertItem> newList) {
        this.alertList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        AlertItem item = alertList.get(position);
        holder.tvName.setText(item.getName());
        holder.tvDate.setText(item.getStatus() + ": " + item.getDate());
        holder.tvType.setText(item.getType() == AlertItem.Type.FOOD ? "Food" : "Medicine");

        // Set Icon based on type
        if (item.getType() == AlertItem.Type.FOOD) {
            holder.ivIcon.setImageResource(R.drawable.vegi_img01); // Default food icon
        } else {
            holder.ivIcon.setImageResource(R.drawable.medsec_image_09); // Default med icon
        }

        // Set color based on status
        int color;
        switch (item.getStatus()) {
            case "Expired":
                color = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark);
                break;
            case "Today":
                color = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark);
                break;
            default: // Soon / This Week
                color = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_blue_dark);
                break;
        }
        holder.tvDate.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvType;
        ImageView ivIcon;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAlertName);
            tvDate = itemView.findViewById(R.id.tvAlertDate);
            tvType = itemView.findViewById(R.id.tvAlertType);
            ivIcon = itemView.findViewById(R.id.ivAlertIcon);
        }
    }
}