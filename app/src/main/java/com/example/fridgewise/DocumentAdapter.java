package com.example.fridgewise;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocViewHolder> {

    public interface OnDocumentClickListener {
        void onEditClick(DocumentItem document);
        void onDeleteClick(DocumentItem document);
    }

    private List<DocumentItem> documentItemList = new ArrayList<>();
    private final OnDocumentClickListener listener;

    public DocumentAdapter(OnDocumentClickListener listener) {
        this.listener = listener;
    }

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

        // Set thumbnail if image exists
        if (currentDoc.getImagePath() != null && !currentDoc.getImagePath().isEmpty()) {
            try {
                holder.ivThumbnail.setImageURI(Uri.parse(currentDoc.getImagePath()));
                holder.ivThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                // This can happen if the stored content:// URI permission has expired
                holder.ivThumbnail.setImageResource(R.drawable.round_camera_alt_24);
                holder.ivThumbnail.setScaleType(ImageView.ScaleType.CENTER);
            }
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.round_camera_alt_24); // Default icon
            holder.ivThumbnail.setScaleType(ImageView.ScaleType.CENTER);
        }

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Edit");
            popup.getMenu().add("Delete");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Edit")) {
                    listener.onEditClick(currentDoc);
                } else if (item.getTitle().equals("Delete")) {
                    listener.onDeleteClick(currentDoc);
                }
                return true;
            });
            popup.show();
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
