package com.example.fridgewise.adapter;

import com.example.fridgewise.R;
import com.example.fridgewise.model.RecipeItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends ListAdapter<RecipeItem, RecipeAdapter.RecipeViewHolder> {

    private final OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(RecipeItem recipe);
    }

    public RecipeAdapter(OnRecipeClickListener listener) {
        super(new DiffUtil.ItemCallback<RecipeItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull RecipeItem oldItem, @NonNull RecipeItem newItem) {
                return oldItem.getName().equals(newItem.getName());
            }

            @Override
            public boolean areContentsTheSame(@NonNull RecipeItem oldItem, @NonNull RecipeItem newItem) {
                String oldTime = oldItem.getTime() != null ? oldItem.getTime() : "";
                String newTime = newItem.getTime() != null ? newItem.getTime() : "";
                return oldTime.equals(newTime) &&
                       oldItem.getMatchPercentage() == newItem.getMatchPercentage();
            }
        });
        this.listener = listener;
    }

    public void setRecipes(List<RecipeItem> recipes) {
        submitList(recipes != null ? new ArrayList<>(recipes) : new ArrayList<>());
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_card, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeItem recipe = getItem(position);
        holder.tvName.setText(recipe.getName());
        holder.tvTime.setText(recipe.getTime());
        holder.tvMatch.setText(recipe.getMatchPercentage() + "% Match");
        holder.ivImage.setImageResource(recipe.getImageResId());
        
        if (recipe.getExtraItemsCount() > 0) {
            holder.tvBadge.setVisibility(View.VISIBLE);
            holder.tvBadge.setText("+" + recipe.getExtraItemsCount() + " Items");
        } else {
            holder.tvBadge.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvTime, tvMatch, tvBadge;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivRecipeImage);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvTime = itemView.findViewById(R.id.tvRecipeTime);
            tvMatch = itemView.findViewById(R.id.tvRecipeMatch);
            tvBadge = itemView.findViewById(R.id.tvRecipeBadge);
        }
    }
}
