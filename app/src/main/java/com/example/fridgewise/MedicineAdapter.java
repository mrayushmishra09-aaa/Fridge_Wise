package com.example.fridgewise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    public interface OnMedicineClickListener {
        void onReminderToggle(MedicineEntity medicine, boolean isChecked);
        void onTakeDose(MedicineEntity medicine);
        void onEditClick(MedicineEntity medicine);
        void onDeleteClick(MedicineEntity medicine);
    }

    private List<MedicineEntity> medicineList;
    private OnMedicineClickListener listener;

    public MedicineAdapter(List<MedicineEntity> medicineList, OnMedicineClickListener listener) {
        this.medicineList = medicineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        MedicineEntity medicine = medicineList.get(position);
        holder.tvTitle.setText(medicine.getMedicineName());
        holder.tvSubtitle.setText(medicine.getDosage() + " " + medicine.getUnit() + " · " + medicine.getFrequency());
        holder.tvTime.setText(medicine.getStartTime());
        holder.ivIcon.setImageResource(medicine.getIconResId());
        
        // Handle "Taken" state
        String today = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());
        boolean isTakenToday = today.equals(medicine.getLastTakenDate());
        
        if (isTakenToday) {
            holder.btnTakeDose.setEnabled(false);
            holder.btnTakeDose.setText("Taken");
            holder.tvStatusChip.setText("Completed");
            holder.tvStatusChip.setBackgroundResource(R.drawable.bg_badge_purple); // Use a light purple/gray badge
            holder.tvStatusChip.setTextColor(holder.itemView.getContext().getColor(R.color.text_gray));
            
            // Faded/Grayed out effect
            holder.itemView.setAlpha(0.6f);
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.btnTakeDose.setEnabled(true);
            holder.btnTakeDose.setText("Take Dose");
            holder.tvStatusChip.setText("Next dose");
            holder.tvStatusChip.setBackgroundResource(R.drawable.bg_chip_light_purple);
            holder.tvStatusChip.setTextColor(holder.itemView.getContext().getColor(R.color.purple_primary));
            
            // Normal state
            holder.itemView.setAlpha(1.0f);
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Low stock warning
        try {
            double qty = Double.parseDouble(medicine.getQuantity());
            if (qty < 5) {
                holder.tvStatusChip.setText("Low Stock: " + medicine.getQuantity());
                holder.tvStatusChip.setBackgroundResource(R.drawable.bg_priority_high);
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }

        holder.btnTakeDose.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTakeDose(medicine);
            }
        });

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMore);
            popup.inflate(R.menu.menu_todo_item);
            popup.setOnMenuItemClickListener(menuItem -> {
                if (listener == null) return false;
                int id = menuItem.getItemId();
                if (id == R.id.menu_edit) {
                    listener.onEditClick(medicine);
                    return true;
                } else if (id == R.id.menu_delete) {
                    listener.onDeleteClick(medicine);
                    return true;
                }
                return false;
            });
            popup.show();
        });
        
        // Remove listener before setting checked to avoid triggering it
        holder.switchMedicine.setOnCheckedChangeListener(null);
        holder.switchMedicine.setChecked(medicine.isReminderOn());
        
        holder.switchMedicine.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onReminderToggle(medicine, isChecked);
            }
        });
        
        // Remove tint to show original icon colors
        holder.ivIcon.setImageTintList(null);
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public void updateList(List<MedicineEntity> newList) {
        this.medicineList = newList;
        notifyDataSetChanged();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvSubtitle, tvTime, tvStatusChip;
        SwitchMaterial switchMedicine;
        MaterialButton btnTakeDose;
        ImageButton btnMore;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivMedicineIcon);
            tvTitle = itemView.findViewById(R.id.tvMedicineTitle);
            tvSubtitle = itemView.findViewById(R.id.tvMedicineSubtitle);
            tvTime = itemView.findViewById(R.id.tvMedicineTime);
            tvStatusChip = itemView.findViewById(R.id.tvStatusChip);
            switchMedicine = itemView.findViewById(R.id.switchMedicine);
            btnTakeDose = itemView.findViewById(R.id.btnTakeDose);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
