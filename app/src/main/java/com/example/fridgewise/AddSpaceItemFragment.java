package com.example.fridgewise;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Calendar;
import java.util.concurrent.Executors;

public class AddSpaceItemFragment extends Fragment {

    private static final String ARG_SPACE_ID = "arg_space_id";
    private static final String ARG_ITEM = "arg_item";

    private int spaceId;
    private CustomSpace parentSpace;
    private CustomSpaceItem existingItem;
    private EditText etName, etQuantity, etUnit, etDate, etReminder, etNotes;
    private TextView tvFileName;
    private View layoutTracking, layoutAttachments, btnRemoveFile;
    private String documentUri = null;
    private String documentName = null;
    private Long selectedReminderTimestamp = null;

    private final androidx.activity.result.ActivityResultLauncher<String[]> filePickerLauncher = 
        registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                documentUri = uri.toString();
                documentName = getFileName(uri);
                tvFileName.setText(documentName);
                btnRemoveFile.setVisibility(View.VISIBLE);
            }
        });

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
        layoutTracking = view.findViewById(R.id.layoutTracking);
        layoutAttachments = view.findViewById(R.id.layoutAttachments);
        tvFileName = view.findViewById(R.id.tvFileName);
        btnRemoveFile = view.findViewById(R.id.btnRemoveFile);
        TextView tvTitle = view.findViewById(R.id.tvHeaderTitle);

        loadSpaceSettings();

        if (existingItem != null) {
            tvTitle.setText("Edit Space Item");
            etName.setText(existingItem.getName());
            etQuantity.setText(String.valueOf(existingItem.getQuantity()));
            etUnit.setText(existingItem.getUnit());
            etDate.setText(existingItem.getDate());
            
            selectedReminderTimestamp = existingItem.getReminderTimestamp();
            if (selectedReminderTimestamp != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault());
                etReminder.setText(sdf.format(new java.util.Date(selectedReminderTimestamp)));
            }
            
            etNotes.setText(existingItem.getNotes());
            documentName = existingItem.getDocumentName();
            if (documentName != null) {
                tvFileName.setText(documentName);
                btnRemoveFile.setVisibility(View.VISIBLE);
            }
        }

        etDate.setOnClickListener(v -> showDatePicker());
        etReminder.setOnClickListener(v -> showCombinedDateTimePicker());
        
        view.findViewById(R.id.btnAttachFile).setOnClickListener(v -> {
            filePickerLauncher.launch(new String[]{"application/pdf", "application/msword", "text/plain", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"});
        });

        btnRemoveFile.setOnClickListener(v -> {
            documentUri = null;
            documentName = null;
            tvFileName.setText("Attach PDF or Document");
            btnRemoveFile.setVisibility(View.GONE);
        });

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> saveItem());

        return view;
    }

    private void loadSpaceSettings() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            parentSpace = db.customSpaceDao().getSpaceById(spaceId);
            if (isAdded() && parentSpace != null) {
                requireActivity().runOnUiThread(() -> {
                    layoutTracking.setVisibility(parentSpace.isHasQuantity() || parentSpace.isHasDate() || parentSpace.isHasReminder() ? View.VISIBLE : View.GONE);
                    layoutAttachments.setVisibility(parentSpace.isHasAttachments() ? View.VISIBLE : View.GONE);
                    
                    etQuantity.setVisibility(parentSpace.isHasQuantity() ? View.VISIBLE : View.GONE);
                    etUnit.setVisibility(parentSpace.isHasQuantity() ? View.VISIBLE : View.GONE);
                    etDate.setVisibility(parentSpace.isHasDate() ? View.VISIBLE : View.GONE);
                    etReminder.setVisibility(parentSpace.isHasReminder() ? View.VISIBLE : View.GONE);
                    etNotes.setVisibility(parentSpace.isHasNotes() ? View.VISIBLE : View.GONE);
                    
                    // Update labels or hints if needed
                    if (parentSpace.isHasCheckbox() && !parentSpace.isHasQuantity()) {
                        etName.setHint("e.g. Finish assignment");
                    }
                });
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String date = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            etDate.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showCombinedDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedReminderTimestamp != null) {
            calendar.setTimeInMillis(selectedReminderTimestamp);
        }

        DatePickerDialog dateDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            android.app.TimePickerDialog timeDialog = new android.app.TimePickerDialog(requireContext(), (v, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                selectedReminderTimestamp = calendar.getTimeInMillis();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault());
                etReminder.setText(sdf.format(calendar.getTime()));

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
            timeDialog.show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dateDialog.show();
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
        String notes = etNotes.getText().toString().trim();

        CustomSpaceItem item = new CustomSpaceItem(spaceId, name, quantity, unit, date, selectedReminderTimestamp, notes);
        item.setDocumentUri(documentUri);
        item.setDocumentName(documentName);
        
        if (existingItem != null) {
            item.setId(existingItem.getId());
            item.setChecked(existingItem.isChecked());
            item.setCompletionTimestamp(existingItem.getCompletionTimestamp());
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            long rowId;
            if (existingItem != null) {
                db.customSpaceDao().updateItem(item);
                rowId = item.getId();
                db.activityDao().insert(new ActivityRecord("Custom Space", "Updated", name, System.currentTimeMillis(), R.drawable.ic_sparkle));
            } else {
                rowId = db.customSpaceDao().insertItem(item);
                db.activityDao().insert(new ActivityRecord("Custom Space", "Added", name, System.currentTimeMillis(), R.drawable.ic_sparkle));
            }

            if (selectedReminderTimestamp != null && selectedReminderTimestamp > System.currentTimeMillis()) {
                scheduleReminder(rowId, name, selectedReminderTimestamp);
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> getParentFragmentManager().popBackStack());
            }
        });
    }

    private void scheduleReminder(long itemId, String itemName, long timeInMillis) {
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) requireContext().getSystemService(android.content.Context.ALARM_SERVICE);
        android.content.Intent intent = new android.content.Intent(requireContext(), ReminderReceiver.class);
        intent.putExtra("item_name", itemName);
        intent.putExtra("space_name", parentSpace != null ? parentSpace.getName() : "Custom Space");
        intent.putExtra("item_id", (int) itemId);

        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                requireContext(),
                (int) itemId,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            } else {
                alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            }
        } catch (SecurityException e) {
            alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }

    private String getFileName(android.net.Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            android.database.Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }
}
