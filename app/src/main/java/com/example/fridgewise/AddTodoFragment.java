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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

/**
 * A fragment that allows users to add a new To-Do item.
 * It provides inputs for title, date, priority, and time.
 */
public class AddTodoFragment extends Fragment {

    public static final String ARG_TODO_ITEM = "todo_item";

    private String selectedPriority = "Medium";
    private String selectedDate = "";
    private String selectedTime = "";
    private TodoItem existingTask;

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

        if (getArguments() != null) {
            existingTask = (TodoItem) getArguments().getSerializable(ARG_TODO_ITEM);
        }

        // Initialize UI components
        ImageView btnBack = view.findViewById(R.id.btnBack);
        Button btnSave = view.findViewById(R.id.btnSave);
        EditText etTitle = view.findViewById(R.id.etTaskTitle);
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);

        TextView btnPriorityHigh = view.findViewById(R.id.btnPriorityHigh);
        TextView btnPriorityMedium = view.findViewById(R.id.btnPriorityMedium);
        TextView btnPriorityLow = view.findViewById(R.id.btnPriorityLow);

        EditText etNote = view.findViewById(R.id.etNote);
        SwitchCompat switchReminder = view.findViewById(R.id.switchReminder);
        TextView tvTime = view.findViewById(R.id.tvTime);

        // Pre-fill fields if editing
        if (existingTask != null) {
            tvHeaderTitle.setText("Edit To-Do");
            btnSave.setText("Update To-Do");
            etTitle.setText(existingTask.getTitle());
            selectedDate = existingTask.getDate();
            tvDate.setText(selectedDate);
            tvDate.setTextColor(getResources().getColor(R.color.text_dark));
            selectedTime = existingTask.getTime();
            tvTime.setText(selectedTime);
            tvTime.setTextColor(getResources().getColor(R.color.text_dark));
            selectedPriority = existingTask.getPriority();
            etNote.setText(existingTask.getNote());
            switchReminder.setChecked(existingTask.isReminderSet());

            // Update priority selection UI
            btnPriorityHigh.setSelected("High".equalsIgnoreCase(selectedPriority));
            btnPriorityMedium.setSelected("Medium".equalsIgnoreCase(selectedPriority));
            btnPriorityLow.setSelected("Low".equalsIgnoreCase(selectedPriority));
        } else {
            // Set default priority selection for new task
            btnPriorityMedium.setSelected(true);
        }

        // Handle Save button click
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                etTitle.setError("Title is required");
                return;
            }

            String note = etNote != null ? etNote.getText().toString().trim() : "";
            boolean isReminderSet = switchReminder != null && switchReminder.isChecked();

            if (existingTask != null) {
                // Update existing task
                existingTask.setTitle(title);
                existingTask.setDate(selectedDate);
                existingTask.setTime(selectedTime);
                existingTask.setPriority(selectedPriority);
                existingTask.setNote(note);
                existingTask.setReminderSet(isReminderSet);

                new Thread(() -> {
                    AppDatabase.getInstance(requireContext()).todoDao().update(existingTask);
                    
                    if (existingTask.isReminderSet()) {
                        scheduleTodoNotification(existingTask);
                    } else {
                        NotificationHelper.cancelNotification(requireContext(), existingTask.getId() + 10000); // Unique offset for todos
                    }

                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Task updated successfully!", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    });
                }).start();
            } else {
                // Save new task
                TodoItem newTask = new TodoItem(title, selectedDate, selectedTime, selectedPriority, note, isReminderSet, false);
                new Thread(() -> {
                    long id = AppDatabase.getInstance(requireContext()).todoDao().insert(newTask);
                    newTask.setId((int) id);

                    if (newTask.isReminderSet()) {
                        scheduleTodoNotification(newTask);
                    }

                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Task added successfully!", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    });
                }).start();
            }
        });

        // Handle Back Button
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Handle Date Selection
        tvDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view1, year1, monthOfYear, dayOfMonth) -> {
                selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                tvDate.setText(selectedDate);
                tvDate.setTextColor(getResources().getColor(R.color.text_dark));
            }, year, month, day);
            datePickerDialog.show();
        });

        // Handle Time Selection
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

        // Handle Priority Selection
        View.OnClickListener priorityListener = v -> {
            // Deselect all
            btnPriorityHigh.setSelected(false);
            btnPriorityMedium.setSelected(false);
            btnPriorityLow.setSelected(false);

            // Select the clicked one
            v.setSelected(true);

            if (v.getId() == R.id.btnPriorityHigh) {
                selectedPriority = "High";
            } else if (v.getId() == R.id.btnPriorityMedium) {
                selectedPriority = "Medium";
            } else if (v.getId() == R.id.btnPriorityLow) {
                selectedPriority = "Low";
            }
        };

        btnPriorityHigh.setOnClickListener(priorityListener);
        btnPriorityMedium.setOnClickListener(priorityListener);
        btnPriorityLow.setOnClickListener(priorityListener);

        return view;
    }

    private void scheduleTodoNotification(TodoItem task) {
        if (task.getDate().isEmpty() || task.getTime().isEmpty()) return;
        try {
            String dateTimeStr = task.getDate() + " " + task.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
            Date date = sdf.parse(dateTimeStr);
            if (date != null) {
                NotificationHelper.scheduleNotification(requireContext(), date.getTime(),
                        "Task Reminder: " + task.getTitle(),
                        "Priority: " + task.getPriority() + (task.getNote().isEmpty() ? "" : " - " + task.getNote()),
                        task.getId() + 10000,
                        R.drawable.ic_todo_item,
                        "TODO",
                        null,
                        "group_todo");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
