package com.example.fridgewise;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private List<TodoItem> todoItems;
    private OnTodoItemClickListener listener;

    public interface OnTodoItemClickListener {
        void onEditClick(TodoItem item);
        void onDeleteClick(TodoItem item);
        void onStatusChange(TodoItem item , boolean isCompleted);
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

        // Handle CheckBox state
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
        
        // Change priority color based on level
        if ("High".equalsIgnoreCase(item.getPriority())) {
            holder.tvPriority.setBackgroundResource(R.drawable.bg_priority_high);
            holder.tvPriority.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red_expired));
        } else if ("Medium".equalsIgnoreCase(item.getPriority())) {
            holder.tvPriority.setBackgroundResource(R.drawable.bg_priority_medium);
            holder.tvPriority.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.orange_warning));
        } else {
            holder.tvPriority.setBackgroundResource(R.drawable.bg_priority_low);
            holder.tvPriority.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_fresh));
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

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTime = itemView.findViewById(R.id.tvTaskTime);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            cbTask = itemView.findViewById(R.id.cbTask);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
