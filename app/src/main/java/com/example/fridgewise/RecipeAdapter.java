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

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<RecipeItem> recipes = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(RecipeItem recipe);
    }

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void setRecipes(List<RecipeItem> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_card, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeItem recipe = recipes.get(position);
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

    @Override
    public int getItemCount() {
        return recipes.size();
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
