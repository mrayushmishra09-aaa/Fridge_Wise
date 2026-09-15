package com.example.fridgewise.adapter;

import com.example.fridgewise.R;
import com.example.fridgewise.model.TodoItem;

import com.example.fridgewise.util.CategoryUtils;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.core.content.ContextCompat;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;
import android.os.Build;
import com.google.android.material.card.MaterialCardView;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private List<TodoItem> todoItems;
    private OnTodoItemClickListener listener;
    private final Set<Integer> selectedIds = new HashSet<>();
    private boolean isSelectionMode = false;

    public interface OnTodoItemClickListener {
        void onEditClick(TodoItem item);
        void onDeleteClick(TodoItem item);
        void onStatusChange(TodoItem item , boolean isCompleted);
        void onSelectionModeChanged(boolean isSelectionMode);
        void onSelectionCountChanged(int count);
    }

    public TodoAdapter(List<TodoItem> todoItems) {
        this.todoItems = todoItems;
    }

    public void setOnTodoItemClickListener(OnTodoItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<TodoItem> newList) {
        this.todoItems = new ArrayList<>(newList);
        Collections.sort(this.todoItems, (t1, t2) -> {
            return getPriorityValue(t2.getPriority()) - getPriorityValue(t1.getPriority());
        });
        notifyDataSetChanged();
    }

    public List<TodoItem> getTodoItems() {
        return todoItems;
    }

    private int getPriorityValue(String priority) {
        if ("High".equalsIgnoreCase(priority)) return 3;
        if ("Medium".equalsIgnoreCase(priority)) return 2;
        return 1;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_task, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoItem item = todoItems.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvTime.setText(item.getTime());
        holder.tvPriority.setText(item.getPriority());

        // --- Selection Logic (Tint Based) ---
        boolean isSelected = selectedIds.contains(item.getId());
        
        if (isSelected) {
            holder.todoCard.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.card_green));
            holder.todoCard.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_primary));
            holder.todoCard.setStrokeWidth(4);
        } else {
            holder.todoCard.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.card_bg));
            holder.todoCard.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.divider_color));
            holder.todoCard.setStrokeWidth(1);
        }

        // Hide completion checkbox during selection mode
        holder.cbTask.setVisibility(isSelectionMode ? View.GONE : View.VISIBLE);
        holder.cbTask.setOnCheckedChangeListener(null);
        holder.cbTask.setChecked(item.isCompleted());
        updateVisualState(holder, item.isCompleted());

        holder.cbTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setCompleted(isChecked);
            updateVisualState(holder, isChecked);
            if (listener != null) {
                listener.onStatusChange(item, isChecked);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(item.getId());
            } else {
                if (listener != null) listener.onEditClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                isSelectionMode = true;
                vibrate(v.getContext());
                toggleSelection(item.getId());
                if (listener != null) listener.onSelectionModeChanged(true);
                return true;
            }
            return false;
        });
        
        // Change priority color based on level
        if ("High".equalsIgnoreCase(item.getPriority())) {
            holder.tvPriority.setBackgroundResource(R.drawable.bg_priority_high);
            holder.tvPriority.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.todo_priority_high_text));
        } else if ("Medium".equalsIgnoreCase(item.getPriority())) {
            holder.tvPriority.setBackgroundResource(R.drawable.bg_priority_medium);
            holder.tvPriority.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.todo_priority_medium_text));
        } else {
            holder.tvPriority.setBackgroundResource(R.drawable.bg_priority_low);
            holder.tvPriority.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.todo_priority_low_text));
        }

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMore);
            popup.inflate(R.menu.menu_todo_item);
            popup.setOnMenuItemClickListener(menuItem -> {
                if (listener == null) return false;
                
                int id = menuItem.getItemId();
                if (id == R.id.menu_edit) {
                    listener.onEditClick(item);
                    return true;
                } else if (id == R.id.menu_delete) {
                    listener.onDeleteClick(item);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return todoItems.size();
    }

    private void toggleSelection(int id) {
        if (selectedIds.contains(id)) selectedIds.remove(id);
        else selectedIds.add(id);
        
        if (selectedIds.isEmpty()) {
            isSelectionMode = false;
            if (listener != null) listener.onSelectionModeChanged(false);
        }
        
        if (listener != null) listener.onSelectionCountChanged(selectedIds.size());
        notifyDataSetChanged();
    }

    public void selectAll() {
        for (TodoItem item : todoItems) {
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

    public List<TodoItem> getSelectedItems() {
        List<TodoItem> selected = new ArrayList<>();
        for (TodoItem item : todoItems) {
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

    private void updateVisualState(TodoViewHolder holder, boolean isCompleted) {
        if (isCompleted) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemView.setAlpha(0.6f);
            holder.tvPriority.setVisibility(View.GONE);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.itemView.setAlpha(1.0f);
            holder.tvPriority.setVisibility(View.VISIBLE);
        }
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvPriority;
        CheckBox cbTask;
        ImageButton btnMore;
        MaterialCardView todoCard;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTime = itemView.findViewById(R.id.tvTaskTime);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            cbTask = itemView.findViewById(R.id.cbTask);
            btnMore = itemView.findViewById(R.id.btnMore);
            todoCard = itemView.findViewById(R.id.todoCard);
        }
    }
}
