package com.example.fridgewise;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Objects;

/**
 * A fragment that allows users to add a new To-Do item.
 * It provides inputs for title, date, priority, and time.
 */
public class AddTodoFragment extends Fragment {

    private String selectedPriority = "Medium";
    private String selectedDate = "";
    private String selectedTime = "";

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return Return the View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_todo, container, false);

        // Initialize UI components
        ImageView btnBack = view.findViewById(R.id.btnBack);
        Button btnSave = view.findViewById(R.id.btnSave);
        EditText etTitle = view.findViewById(R.id.etTaskTitle);
        TextView tvDate = view.findViewById(R.id.tvDate);

        TextView btnPriorityHigh = view.findViewById(R.id.btnPriorityHigh);
        TextView btnPriorityMedium = view.findViewById(R.id.btnPriorityMedium);
        TextView btnPriorityLow = view.findViewById(R.id.btnPriorityLow);

        // Handle back button click
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Handle Date Selection
        tvDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year1, month1, dayOfMonth1) -> {
                        selectedDate = dayOfMonth1 + "/" + (month1 + 1) + "/" + year1;
                        tvDate.setText(selectedDate);
                        tvDate.setTextColor(getResources().getColor(R.color.text_dark));
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Handle Priority Selection
        View.OnClickListener priorityClickListener = v -> {
            btnPriorityHigh.setSelected(false);
            btnPriorityMedium.setSelected(false);
            btnPriorityLow.setSelected(false);

            v.setSelected(true);

            if (v.getId() == R.id.btnPriorityHigh) selectedPriority = "High";
            else if (v.getId() == R.id.btnPriorityMedium) selectedPriority = "Medium";
            else if (v.getId() == R.id.btnPriorityLow) selectedPriority = "Low";
        };

        btnPriorityHigh.setOnClickListener(priorityClickListener);
        btnPriorityMedium.setOnClickListener(priorityClickListener);
        btnPriorityLow.setOnClickListener(priorityClickListener);

        // Set default priority selection
        btnPriorityMedium.setSelected(true);

        // Handle Save button click
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                etTitle.setError("Title is required");
                return;
            }

            EditText etNote = view.findViewById(R.id.etNote);
            SwitchCompat switchReminder = view.findViewById(R.id.switchReminder);

            String note = etNote != null ? etNote.getText().toString().trim() : "";
            boolean isReminderSet = switchReminder != null && switchReminder.isChecked();

            TodoItem newTask = new TodoItem(title, selectedDate, selectedTime, selectedPriority, note, isReminderSet, false);

            // Save to Database in background
            new Thread(() -> {
                AppDatabase.getInstance(requireContext()).todoDao().insert(newTask);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Task added successfully!", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
            }).start();
        });

        // Handle Time Selection
        TextView tvTime = view.findViewById(R.id.tvTime);
        if (tvTime != null) {
            tvTime.setOnClickListener(v -> {
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view1, hourOfDay, minuteOfHour) -> {
                    selectedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                    tvTime.setText(selectedTime);
                    tvTime.setTextColor(getResources().getColor(R.color.text_dark));
                }, hour, minute, false);
                timePickerDialog.show();
            });
        }

        return view;
    }
}
