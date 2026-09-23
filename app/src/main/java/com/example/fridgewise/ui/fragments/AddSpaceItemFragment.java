package com.example.fridgewise.ui.fragments;

import com.example.fridgewise.R;
import com.example.fridgewise.data.*;
import com.example.fridgewise.model.*;
import com.example.fridgewise.adapter.*;
import com.example.fridgewise.util.*;
import com.example.fridgewise.ui.viewmodel.*;
import com.example.fridgewise.ui.activities.*;
import com.example.fridgewise.ui.bottomsheet.*;
import androidx.navigation.Navigation;

import com.google.android.material.switchmaterial.SwitchMaterial;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.transition.TransitionManager;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private View layoutTracking, layoutAttachments, btnRemoveFile;
    private TextView tvFileName;
    private SwitchMaterial switchReminder;
    private String documentUri = null;
    private String documentName = null;
    private String documentMimeType = null;
    private Long selectedReminderTimestamp = null;

    private final ActivityResultLauncher<String> filePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                saveFileLocally(uri);
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
        
        switchReminder = view.findViewById(R.id.switchReminder);

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
                switchReminder.setChecked(true);
                view.findViewById(R.id.tilReminder).setVisibility(View.VISIBLE);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
                etReminder.setText(sdf.format(new Date(selectedReminderTimestamp)));
            }
            
            etNotes.setText(existingItem.getNotes());
            documentUri = existingItem.getDocumentUri();
            documentName = existingItem.getDocumentName();
            documentMimeType = existingItem.getDocumentMimeType();
            if (documentName != null) {
                tvFileName.setText(documentName);
                btnRemoveFile.setVisibility(View.VISIBLE);
            }
        }

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            TransitionManager.beginDelayedTransition((ViewGroup) view);
            view.findViewById(R.id.tilReminder).setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                selectedReminderTimestamp = null;
                etReminder.setText("");
            }
        });

        etDate.setOnClickListener(v -> showDatePicker());
        etReminder.setOnClickListener(v -> showCombinedDateTimePicker());
        
        view.findViewById(R.id.btnAttachFile).setOnClickListener(v -> {
            filePickerLauncher.launch("*/*");
        });

        btnRemoveFile.setOnClickListener(v -> {
            documentUri = null;
            documentName = null;
            tvFileName.setText("No file attached");
            btnRemoveFile.setVisibility(View.GONE);
        });

        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> saveItem());

        return view;
    }

    private void loadSpaceSettings() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            parentSpace = db.customSpaceDao().getSpaceByIdSync(spaceId);
            if (isAdded() && parentSpace != null) {
                requireActivity().runOnUiThread(() -> {
                    if (getView() == null) return;
                    
                    // Animate visibility changes
                    TransitionManager.beginDelayedTransition((ViewGroup) getView());

                    // Visibility logic for flat rows
                    boolean hasQuantity = parentSpace.isHasQuantity();
                    getView().findViewById(R.id.divider1).setVisibility(hasQuantity ? View.VISIBLE : View.GONE);
                    getView().findViewById(R.id.layoutQuantityRow).setVisibility(hasQuantity ? View.VISIBLE : View.GONE);
                    
                    boolean hasTracking = parentSpace.isHasDate() || parentSpace.isHasReminder();
                    layoutTracking.setVisibility(hasTracking ? View.VISIBLE : View.GONE);
                    getView().findViewById(R.id.layoutTrackingDivider).setVisibility(hasTracking ? View.VISIBLE : View.GONE);
                    
                    getView().findViewById(R.id.tvLabelDate).setVisibility(parentSpace.isHasDate() ? View.VISIBLE : View.GONE);
                    getView().findViewById(R.id.containerDate).setVisibility(parentSpace.isHasDate() ? View.VISIBLE : View.GONE);
                    
                    View reminderToggle = getView().findViewById(R.id.layoutReminderToggle);
                    reminderToggle.setVisibility(parentSpace.isHasReminder() ? View.VISIBLE : View.GONE);
                    
                    // Adjust margin for reminder if date is missing to prevent gaps
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) reminderToggle.getLayoutParams();
                    params.topMargin = parentSpace.isHasDate() ? (int) (20 * getResources().getDisplayMetrics().density) : 0;
                    reminderToggle.setLayoutParams(params);
                    
                    layoutAttachments.setVisibility(parentSpace.isHasAttachments() ? View.VISIBLE : View.GONE);
                    getView().findViewById(R.id.layoutAttachmentsDivider).setVisibility(parentSpace.isHasAttachments() ? View.VISIBLE : View.GONE);

                    View layoutNotes = getView().findViewById(R.id.layoutNotes);
                    if (layoutNotes != null) {
                        layoutNotes.setVisibility(parentSpace.isHasNotes() ? View.VISIBLE : View.GONE);
                    }

                    // Dynamic Hints for Item Name
                    String spaceName = parentSpace.getName().toLowerCase();
                    if (spaceName.contains("tool")) {
                        etName.setHint("Tool Name (e.g. Electric Drill)");
                    } else if (spaceName.contains("grocery") || spaceName.contains("fridge")) {
                        etName.setHint("Item Name (e.g. Organic Milk)");
                    } else if (spaceName.contains("pet")) {
                        etName.setHint("Pet Item (e.g. Dog Food)");
                    } else if (spaceName.contains("doc") || spaceName.contains("file")) {
                        etName.setHint("Document Name (e.g. Health Insurance)");
                    } else if (parentSpace.isHasCheckbox()) {
                        etName.setHint("Task Name");
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
        item.setDocumentMimeType(documentMimeType);
        
        // Safety: default to unchecked when creating/editing from form per user request
        if (existingItem != null) {
            item.setChecked(existingItem.isChecked());
            item.setCompletionTimestamp(existingItem.getCompletionTimestamp());
            item.setId(existingItem.getId());
        } else {
            item.setChecked(false);
            item.setCompletionTimestamp(null);
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
                requireActivity().runOnUiThread(() -> {
                    if (getView() != null) Navigation.findNavController(getView()).popBackStack();
                });
            }
        });
    }

    private void scheduleReminder(long itemId, String itemName, long timeInMillis) {
        ReminderCoordinator coordinator = new ReminderCoordinator(requireContext());
        coordinator.schedule("SPACE", (int) itemId,
                "Space Reminder",
                itemName,
                timeInMillis,
                R.drawable.ic_sparkle,
                "group_space");
    }

    private void saveFileLocally(Uri uri) {
        Context context = getContext();
        if (context == null) return;
        
        new Thread(() -> {
            String name = FileUtil.getFileName(context, uri);
            String mime = FileUtil.getMimeType(context, uri);
            String localPath = FileUtil.saveToInternalStorage(context, uri);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!localPath.isEmpty()) {
                        documentUri = localPath;
                        documentName = name;
                        documentMimeType = mime;
                        tvFileName.setText(documentName);
                        btnRemoveFile.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(context, "Error attaching file", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
}
