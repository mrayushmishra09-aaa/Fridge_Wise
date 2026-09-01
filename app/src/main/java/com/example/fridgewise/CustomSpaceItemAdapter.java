package com.example.fridgewise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CustomSpaceItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private List<CustomSpaceItem> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private CustomSpace parentSpace;

    public interface OnItemClickListener {
        void onItemClick(CustomSpaceItem item);
        void onDeleteClick(CustomSpaceItem item);
        void onCheckChanged(CustomSpaceItem item, boolean isChecked);
    }

    public CustomSpaceItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CustomSpaceItem> items, CustomSpace parentSpace) {
        this.items = items;
        this.parentSpace = parentSpace;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (parentSpace != null && parentSpace.getAutoRemoveDuration() > 0 && position == items.size()) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_auto_remove_footer, parent, false);
            return new FooterViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_space_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterViewHolder) {
            FooterViewHolder footer = (FooterViewHolder) holder;
            if (parentSpace != null) {
                String duration = parentSpace.getAutoRemoveDuration() == 1 ? "24 hours" : "7 days";
                footer.tvMsg.setText("Completed items will be automatically moved here and deleted after " + duration + " (you can change this in settings).");
            }
            return;
        }

        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        CustomSpaceItem item = items.get(position);
        itemHolder.tvName.setText(item.getName());
        
        if (parentSpace == null) return;

        // 1. Checkbox / Checklist capability
        if (parentSpace.isHasCheckbox()) {
            itemHolder.checkBox.setVisibility(View.VISIBLE);
            itemHolder.checkBox.setOnCheckedChangeListener(null);
            itemHolder.checkBox.setChecked(item.isChecked());
            
            // Visual feedback for completion (Matching the Image Design)
            if (item.isChecked()) {
                // Strike-through and Mute Title
                itemHolder.tvName.setPaintFlags(itemHolder.tvName.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                itemHolder.tvName.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));
                
                // Mute Notes
                itemHolder.tvNotes.setTextColor(android.graphics.Color.parseColor("#BDBDBD"));
                
                // Show Completed Tag & Footer
                itemHolder.tagStatus.setVisibility(View.VISIBLE);
                itemHolder.tvCompletionFooter.setVisibility(View.VISIBLE);
                
                if (item.getCompletionTimestamp() != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault());
                    String time = sdf.format(new java.util.Date(item.getCompletionTimestamp()));
                    itemHolder.tvCompletionFooter.setText("Completed on " + time);
                }
                
                // Mute Card Background
                itemHolder.cardMain.setCardBackgroundColor(android.graphics.Color.parseColor("#FAFAFA"));
            } else {
                itemHolder.tvName.setPaintFlags(itemHolder.tvName.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                itemHolder.tvName.setTextColor(android.graphics.Color.BLACK);
                itemHolder.tvNotes.setTextColor(android.graphics.Color.parseColor("#757575"));
                
                itemHolder.tagStatus.setVisibility(View.GONE);
                itemHolder.tvCompletionFooter.setVisibility(View.GONE);
                itemHolder.cardMain.setCardBackgroundColor(android.graphics.Color.WHITE);
            }

            itemHolder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                listener.onCheckChanged(item, isChecked);
            });
        } else {
            itemHolder.checkBox.setVisibility(View.GONE);
            itemHolder.tagStatus.setVisibility(View.GONE);
            itemHolder.tvCompletionFooter.setVisibility(View.GONE);
            itemHolder.tvName.setPaintFlags(itemHolder.tvName.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            itemHolder.tvName.setTextColor(android.graphics.Color.BLACK);
            itemHolder.cardMain.setCardBackgroundColor(android.graphics.Color.WHITE);
        }

        // 2. Notes capability
        if (parentSpace.isHasNotes() && item.getNotes() != null && !item.getNotes().isEmpty()) {
            itemHolder.tvNotes.setVisibility(View.VISIBLE);
            itemHolder.tvNotes.setText(item.getNotes());
            itemHolder.tagNotesIcon.setVisibility(View.VISIBLE);
        } else {
            itemHolder.tvNotes.setVisibility(View.GONE);
            itemHolder.tagNotesIcon.setVisibility(View.GONE);
        }

        // 4. Quantity Tag
        if (parentSpace.isHasQuantity() && item.getQuantity() > 0) {
            itemHolder.tagQuantity.setVisibility(View.VISIBLE);
            itemHolder.tvTagQuantity.setText(item.getQuantity() + " " + (item.getUnit() != null ? item.getUnit() : ""));
        } else {
            itemHolder.tagQuantity.setVisibility(View.GONE);
        }

        // 5. Date Tag
        if (parentSpace.isHasDate() && item.getDate() != null && !item.getDate().isEmpty()) {
            itemHolder.tagDate.setVisibility(View.VISIBLE);
            itemHolder.tvTagDate.setText(item.getDate());
        } else {
            itemHolder.tagDate.setVisibility(View.GONE);
        }

        // 6. Reminder Tag
        if (parentSpace.isHasReminder() && item.getReminderTimestamp() != null) {
            itemHolder.tagReminder.setVisibility(View.VISIBLE);
            
            long time = item.getReminderTimestamp();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault());
            itemHolder.tvTagReminder.setText(sdf.format(new java.util.Date(time)));

            // Visual Cue: Overdue or Due Soon
            long now = System.currentTimeMillis();
            if (item.isChecked()) {
                itemHolder.tagReminder.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E3F2FD")));
                itemHolder.tvTagReminder.setTextColor(android.graphics.Color.parseColor("#1976D2"));
            } else if (time < now) {
                // Overdue - Red
                itemHolder.tagReminder.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFEBEE")));
                itemHolder.tvTagReminder.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
            } else if (time - now < 3600000) {
                // Due in < 1 hour - Orange
                itemHolder.tagReminder.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFF3E0")));
                itemHolder.tvTagReminder.setTextColor(android.graphics.Color.parseColor("#E65100"));
            } else {
                // Normal - Blue
                itemHolder.tagReminder.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E3F2FD")));
                itemHolder.tvTagReminder.setTextColor(android.graphics.Color.parseColor("#1976D2"));
            }
        } else {
            itemHolder.tagReminder.setVisibility(View.GONE);
        }

        // 7. Attachment Tag
        if (parentSpace.isHasAttachments() && item.getDocumentName() != null) {
            itemHolder.tagAttachment.setVisibility(View.VISIBLE);
            itemHolder.tvTagAttachment.setText(item.getDocumentName());
        } else {
            itemHolder.tagAttachment.setVisibility(View.GONE);
        }

        // CRITICAL FIX: Hide the entire Tags Container and Progress Section if empty
        boolean anyTagVisible = itemHolder.tagQuantity.getVisibility() == View.VISIBLE ||
                               itemHolder.tagDate.getVisibility() == View.VISIBLE ||
                               itemHolder.tagReminder.getVisibility() == View.VISIBLE ||
                               itemHolder.tagNotesIcon.getVisibility() == View.VISIBLE ||
                               itemHolder.tagAttachment.getVisibility() == View.VISIBLE ||
                               itemHolder.tagStatus.getVisibility() == View.VISIBLE;
        
        itemHolder.tagsLayout.setVisibility(anyTagVisible ? View.VISIBLE : View.GONE);

        itemHolder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        itemHolder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        int count = items.size();
        if (parentSpace != null && parentSpace.getAutoRemoveDuration() > 0) {
            count++;
        }
        return count;
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView tvMsg;
        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMsg = itemView.findViewById(R.id.tvAutoRemoveMsg);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNotes, tvTagQuantity, tvTagDate, tvTagReminder, tvTagStatus, tvTagAttachment, tvCompletionFooter;
        android.widget.CheckBox checkBox;
        View tagsLayout;
        com.google.android.material.card.MaterialCardView cardMain;
        View tagQuantity, tagDate, tagReminder, tagStatus, tagNotesIcon, tagAttachment;
        android.widget.ImageView btnDelete;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMain = (com.google.android.material.card.MaterialCardView) itemView.findViewById(R.id.cardItem);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvNotes = itemView.findViewById(R.id.tvItemNotes);
            tvCompletionFooter = itemView.findViewById(R.id.tvCompletionFooter);
            checkBox = itemView.findViewById(R.id.itemCheckBox);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            
            tagsLayout = itemView.findViewById(R.id.tagsLayout);
            
            tagQuantity = itemView.findViewById(R.id.tagQuantity);
            tvTagQuantity = itemView.findViewById(R.id.tvTagQuantity);
            tagDate = itemView.findViewById(R.id.tagDate);
            tvTagDate = itemView.findViewById(R.id.tvTagDate);
            tagReminder = itemView.findViewById(R.id.tagReminder);
            tvTagReminder = itemView.findViewById(R.id.tvTagReminder);
            tagStatus = itemView.findViewById(R.id.tagStatus);
            tvTagStatus = itemView.findViewById(R.id.tvTagStatus);
            tagNotesIcon = itemView.findViewById(R.id.tagNotesIcon);
            tagAttachment = itemView.findViewById(R.id.tagAttachment);
            tvTagAttachment = itemView.findViewById(R.id.tvTagAttachment);
        }
    }
}
