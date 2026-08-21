package com.example.fridgewise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CustomSpaceAdapter extends RecyclerView.Adapter<CustomSpaceAdapter.SpaceViewHolder> {

    private List<CustomSpace> spaces = new ArrayList<>();
    private final OnSpaceClickListener listener;

    public interface OnSpaceClickListener {
        void onSpaceClick(CustomSpace space);
        void onSpaceLongClick(CustomSpace space, View view);
    }

    public CustomSpaceAdapter(OnSpaceClickListener listener) {
        this.listener = listener;
    }

    public void setSpaces(List<CustomSpace> spaces) {
        this.spaces = spaces;
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
        
        if (space.getImageUri() != null && !space.getImageUri().isEmpty()) {
            // Load custom image if exists (simplified for now)
            // holder.icon.setImageURI(Uri.parse(space.getImageUri()));
        } else {
            holder.icon.setImageResource(space.getIconResId());
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

        public SpaceViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iconSpace);
            tvName = itemView.findViewById(R.id.tvSpaceName);
            tvCount = itemView.findViewById(R.id.tvSpaceItemCount);
        }
    }
}
