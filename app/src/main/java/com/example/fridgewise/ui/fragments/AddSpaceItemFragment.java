package com.example.fridgewise.ui.fragments;

import com.example.fridgewise.R;
import com.example.fridgewise.data.*;
import com.example.fridgewise.model.*;
import com.example.fridgewise.adapter.*;
import com.example.fridgewise.util.*;
import com.example.fridgewise.ui.viewmodel.*;
import com.example.fridgewise.ui.activities.*;
import com.example.fridgewise.ui.bottomsheet.*;

import com.example.fridgewise.R;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class AddSpaceItemFragment extends Fragment {

    private static final String ARG_SPACE_ID = "arg_space_id";
    private static final String ARG_ITEM = "arg_item";

    private int spaceId;
    private CustomSpace parentSpace;
    private CustomSpaceItem existingItem;
    private EditText etName, etQuantity, etUnit, etDate, etReminder, etNotes;
    private TextView tvFileName, tvLabelName, tvLabelQuantity, tvLabelUnit, tvLabelDate, tvLabelReminder, tvLabelNotes, tvLabelAttachments;
    private View layoutTracking, layoutAttachments, btnRemoveFile;
    private String documentUri = null;
    private String documentName = null;
    private Long selectedReminderTimestamp = null;

    private final ActivityResultLauncher<String[]> filePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
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
        
        tvLabelName = view.findViewById(R.id.tvLabelName);
        tvLabelQuantity = view.findViewById(R.id.tvLabelQuantity);
        tvLabelUnit = view.findViewById(R.id.tvLabelUnit);
        tvLabelDate = view.findViewById(R.id.tvLabelDate);
        tvLabelReminder = view.findViewById(R.id.tvLabelReminder);
        tvLabelNotes = view.findViewById(R.id.tvLabelNotes);
        tvLabelAttachments = view.findViewById(R.id.tvLabelAttachments);

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
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
                etReminder.setText(sdf.format(new Date(selectedReminderTimestamp)));
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
                    boolean showTracking = parentSpace.isHasQuantity() || parentSpace.isHasDate() || parentSpace.isHasReminder();
                    layoutTracking.setVisibility(showTracking ? View.VISIBLE : View.GONE);
                    layoutAttachments.setVisibility(parentSpace.isHasAttachments() ? View.VISIBLE : View.GONE);
                    tvLabelAttachments.setVisibility(parentSpace.isHasAttachments() ? View.VISIBLE : View.GONE);
                    
                    etQuantity.setVisibility(parentSpace.isHasQuantity() ? View.VISIBLE : View.GONE);
                    tvLabelQuantity.setVisibility(parentSpace.isHasQuantity() ? View.VISIBLE : View.GONE);
                    
                    etUnit.setVisibility(parentSpace.isHasQuantity() ? View.VISIBLE : View.GONE);
                    tvLabelUnit.setVisibility(parentSpace.isHasQuantity() ? View.VISIBLE : View.GONE);
                    
                    etDate.setVisibility(parentSpace.isHasDate() ? View.VISIBLE : View.GONE);
                    tvLabelDate.setVisibility(parentSpace.isHasDate() ? View.VISIBLE : View.GONE);
                    
                    etReminder.setVisibility(parentSpace.isHasReminder() ? View.VISIBLE : View.GONE);
                    tvLabelReminder.setVisibility(parentSpace.isHasReminder() ? View.VISIBLE : View.GONE);
                    
                    etNotes.setVisibility(parentSpace.isHasNotes() ? View.VISIBLE : View.GONE);
                    tvLabelNotes.setVisibility(parentSpace.isHasNotes() ? View.VISIBLE : View.GONE);
                    
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
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
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

            TimePickerDialog timeDialog = new TimePickerDialog(requireContext(), (v, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                selectedReminderTimestamp = calendar.getTimeInMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
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
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), ReminderReceiver.class);
        intent.putExtra("item_name", itemName);
        intent.putExtra("space_name", parentSpace != null ? parentSpace.getName() : "Custom Space");
        intent.putExtra("item_id", (int) itemId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                (int) itemId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            }
        } catch (SecurityException e) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
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
