package com.example.fridgewise;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Calendar;
import java.util.concurrent.Executors;

public class AddSpaceItemFragment extends Fragment {

    private static final String ARG_SPACE_ID = "arg_space_id";
    private static final String ARG_ITEM = "arg_item";

    private int spaceId;
    private CustomSpaceItem existingItem;
    private EditText etName, etQuantity, etUnit, etDate, etReminder, etNotes;

    public static AddSpaceItemFragment newInstance(int spaceId, @Nullable CustomSpaceItem item) {
        AddSpaceItemFragment fragment = new AddSpaceItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SPACE_ID, spaceId);
        if (item != null) {
            args.putSerializable(ARG_ITEM, item);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            spaceId = getArguments().getInt(ARG_SPACE_ID);
            existingItem = (CustomSpaceItem) getArguments().getSerializable(ARG_ITEM);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_space_item, container, false);

        etName = view.findViewById(R.id.etItemName);
        etQuantity = view.findViewById(R.id.etQuantity);
        etUnit = view.findViewById(R.id.etUnit);
        etDate = view.findViewById(R.id.etDate);
        etReminder = view.findViewById(R.id.etReminder);
        etNotes = view.findViewById(R.id.etNotes);
        TextView tvTitle = view.findViewById(R.id.tvHeaderTitle);

        if (existingItem != null) {
            tvTitle.setText("Edit Space Item");
            etName.setText(existingItem.getName());
            etQuantity.setText(String.valueOf(existingItem.getQuantity()));
            etUnit.setText(existingItem.getUnit());
            etDate.setText(existingItem.getDate());
            etReminder.setText(existingItem.getReminderTime());
            etNotes.setText(existingItem.getNotes());
        }

        etDate.setOnClickListener(v -> showDatePicker());

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        view.findViewById(R.id.btnSave).setOnClickListener(v -> saveItem());

        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String date = year + "-" + (month + 1) + "-" + dayOfMonth;
            etDate.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveItem() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Item name required", Toast.LENGTH_SHORT).show();
            return;
        }

        double quantity = 0;
        try {
            quantity = Double.parseDouble(etQuantity.getText().toString().trim());
        } catch (Exception ignored) {}

        String unit = etUnit.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String reminder = etReminder.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        CustomSpaceItem item = new CustomSpaceItem(spaceId, name, quantity, unit, date, reminder, notes);
        if (existingItem != null) {
            item.setId(existingItem.getId());
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            if (existingItem != null) {
                db.customSpaceDao().updateItem(item);
                db.activityDao().insert(new ActivityRecord("Custom Space", "Updated", name, System.currentTimeMillis(), R.drawable.ic_sparkle));
            } else {
                db.customSpaceDao().insertItem(item);
                db.activityDao().insert(new ActivityRecord("Custom Space", "Added", name, System.currentTimeMillis(), R.drawable.ic_sparkle));
            }
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> getParentFragmentManager().popBackStack());
            }
        });
    }
}
