package com.example.fridgewise.adapter;

import com.example.fridgewise.R;
import com.example.fridgewise.model.DocumentItem;

import android.content.Intent;
import com.example.fridgewise.ui.activities.DocumentViewerActivity;
import com.google.android.material.card.MaterialCardView;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.content.Context;
import android.os.Build;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocViewHolder> {

    public interface OnDocumentClickListener {
        void onEditClick(DocumentItem document);
        void onDeleteClick(DocumentItem document);
        void onDownloadClick(DocumentItem document);
        void onSelectionModeChanged(boolean isSelectionMode);
        void onSelectionCountChanged(int count);
    }

    private List<DocumentItem> documentItemList = new ArrayList<>();
    private final OnDocumentClickListener listener;
    private final Set<Integer> selectedIds = new HashSet<>();
    private boolean isSelectionMode = false;

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
        holder.tvCategory.setText(String.format("Document #%02d", position + 1));

        if (currentDoc.getImagePath() != null && !currentDoc.getImagePath().isEmpty()) {
            try {
                holder.ivThumbnail.setImageURI(Uri.parse(currentDoc.getImagePath()));
                holder.ivThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                holder.ivThumbnail.setImageResource(R.drawable.round_camera_alt_24);
                holder.ivThumbnail.setScaleType(ImageView.ScaleType.CENTER);
            }
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.round_camera_alt_24);
            holder.ivThumbnail.setScaleType(ImageView.ScaleType.CENTER);
        }

        // --- Selection Logic (Tint Based) ---
        boolean isSelected = selectedIds.contains(currentDoc.getId());
        
        if (isSelected) {
            holder.docCard.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.card_green));
            holder.docCard.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_primary));
            holder.docCard.setStrokeWidth(4);
        } else {
            holder.docCard.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.doc_item_card_bg));
            holder.docCard.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.doc_item_card_stroke));
            holder.docCard.setStrokeWidth(3);
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                isSelectionMode = true;
                vibrate(v.getContext());
                toggleSelection(currentDoc.getId());
                if (listener != null) listener.onSelectionModeChanged(true);
                return true;
            }
            return false;
        });

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(currentDoc.getId());
            } else {
                if (currentDoc.getImagePath() != null && !currentDoc.getImagePath().isEmpty()) {
                    Intent intent = new Intent(v.getContext(), DocumentViewerActivity.class);
                    intent.putExtra("imageUri", currentDoc.getImagePath());
                    intent.putExtra("docName", currentDoc.getName());
                    intent.putExtra("docTag", String.format("Document #%02d", position + 1));
                    v.getContext().startActivity(intent);
                }
            }
        });

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Edit");
            popup.getMenu().add("Delete");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Edit")) listener.onEditClick(currentDoc);
                else if (item.getTitle().equals("Delete")) listener.onDeleteClick(currentDoc);
                return true;
            });
            popup.show();
        });

        holder.btnDownload.setOnClickListener(v -> {
            if (listener != null) listener.onDownloadClick(currentDoc);
        });
    }

    private void toggleSelection(int docId) {
        if (selectedIds.contains(docId)) selectedIds.remove(docId);
        else selectedIds.add(docId);
        
        if (selectedIds.isEmpty()) {
            isSelectionMode = false;
            if (listener != null) listener.onSelectionModeChanged(false);
        }
        
        if (listener != null) listener.onSelectionCountChanged(selectedIds.size());
        notifyDataSetChanged();
    }

    public void selectAll() {
        for (DocumentItem item : documentItemList) {
            selectedIds.add(item.getId());
        }
        isSelectionMode = true;
        if (listener != null) {
            listener.onSelectionCountChanged(selectedIds.size());
            listener.onSelectionModeChanged(true);
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedIds.clear();
        isSelectionMode = false;
        if (listener != null) listener.onSelectionCountChanged(0);
        notifyDataSetChanged();
    }

    public List<DocumentItem> getSelectedItems() {
        List<DocumentItem> selected = new ArrayList<>();
        for (DocumentItem item : documentItemList) {
            if (selectedIds.contains(item.getId())) selected.add(item);
        }
        return selected;
    }

    private void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            else vibrator.vibrate(50);
        }
    }

    @Override
    public int getItemCount() { return documentItemList.size(); }

    static class DocViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory;
        ImageView ivThumbnail, btnMore, btnDownload;
        MaterialCardView docCard;

        public DocViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDocName);
            tvCategory = itemView.findViewById(R.id.tvDocCategory);
            ivThumbnail = itemView.findViewById(R.id.ivDocThumbnail);
            btnMore = itemView.findViewById(R.id.btnMore);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            docCard = itemView.findViewById(R.id.docCard);
        }
    }
}
