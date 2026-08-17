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

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocViewHolder> {

    private List<DocumentItem> documentItemList = new ArrayList<>();

    public void setDocs(List<DocumentItem> docs) {
        this.documentItemList = docs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new DocViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocViewHolder holder, int position) {
        DocumentItem currentDoc = documentItemList.get(position);

        holder.tvName.setText(currentDoc.getName());
        holder.tvCategory.setText(currentDoc.getCategory());

        holder.btnMore.setOnClickListener(v -> {
            // Logic for edit/delete will go here
        });
    }

    @Override
    public int getItemCount() {
        return documentItemList.size();
    }

    static class DocViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory;
        ImageView ivThumbnail, btnMore;

        public DocViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDocName);
            tvCategory = itemView.findViewById(R.id.tvDocCategory);
            ivThumbnail = itemView.findViewById(R.id.ivDocThumbnail);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
