package com.example.fridgewise;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.concurrent.Executors;

public class CreateSpaceFragment extends Fragment {

    private EditText etName, etDesc;
    private TextView tvNameCount, tvDescCount;
    private ImageView ivSelectedIcon, ivCustomPhoto, ivAdvancedChevron;
    private LinearLayout layoutAdvanced;
    private View btnOptCheckbox, btnOptReminder, btnOptNotes, btnOptQuantity, btnOptDate, btnOptAttachments;
    private boolean hasCheckbox, hasReminder, hasNotes, hasQuantity, hasDate, hasImage, hasAttachments;
    private android.widget.Spinner spinnerAutoRemove;
    private int selectedIconRes = R.drawable.round_camera_alt_24;
    private int selectedColor = Color.parseColor("#2D6A4F"); // Default Green
    private String privacyStatus = "Private";
    private String customImageUri = null;
    private CustomSpace editingSpace = null;

    private final int[] availableIcons = {
        R.drawable.v02_img_icons_household,
        R.drawable.v02_img_icons_pet,
        R.drawable.v02_img_icons_doccc,
        R.drawable.v02_img_icons_shopping,
        R.drawable.v02_img_icons_to_do,
        R.drawable.v02_img_icons_medicne
    };

    private final String[] availableColors = {
        "#2D6A4F", // Green
        "#4A90E2", // Blue
        "#7B61FF", // Purple
        "#A061FF", // Light Purple
        "#FF6B6B", // Red/Pink
        "#FFB347", // Orange
        "#4ECDC4"  // Cyan
    };

    public static CreateSpaceFragment newInstance(CustomSpace space) {
        CreateSpaceFragment fragment = new CreateSpaceFragment();
        if (space != null) {
            Bundle args = new Bundle();
            args.putSerializable("custom_space", space);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_space, container, false);

        if (getArguments() != null && getArguments().containsKey("custom_space")) {
            editingSpace = (CustomSpace) getArguments().getSerializable("custom_space");
        }

        initializeViews(view);
        setupIconPicker(view);
        setupColorPicker(view);
        setupCharacterCounters();
        setupAdvancedToggle(view);

        if (editingSpace != null) {
            populateEditingData(view);
        }

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        view.findViewById(R.id.btnCreateSpace).setOnClickListener(v -> validateAndSave());
        view.findViewById(R.id.btnChangePhoto).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Photo selection coming soon", Toast.LENGTH_SHORT).show());

        return view;
    }

    private void initializeViews(View view) {
        etName = view.findViewById(R.id.etSpaceName);
        etDesc = view.findViewById(R.id.etSpaceDesc);
        tvNameCount = view.findViewById(R.id.tvNameCount);
        tvDescCount = view.findViewById(R.id.tvDescCount);
        ivSelectedIcon = view.findViewById(R.id.ivSpaceIcon);
        ivCustomPhoto = view.findViewById(R.id.ivCustomPhoto);
        ivAdvancedChevron = view.findViewById(R.id.ivAdvancedChevron);
        layoutAdvanced = view.findViewById(R.id.layoutAdvanced);
        
        btnOptCheckbox = view.findViewById(R.id.btnOptCheckbox);
        btnOptReminder = view.findViewById(R.id.btnOptReminder);
        btnOptNotes = view.findViewById(R.id.btnOptNotes);
        btnOptQuantity = view.findViewById(R.id.btnOptQuantity);
        btnOptDate = view.findViewById(R.id.btnOptDate);
        btnOptAttachments = view.findViewById(R.id.btnOptAttachments);
        
        setupCapabilityButtons();
        
        spinnerAutoRemove = view.findViewById(R.id.spinnerAutoRemove);
    }

    private void setupCapabilityButtons() {
        btnOptCheckbox.setOnClickListener(v -> toggleCapability("checkbox"));
        btnOptReminder.setOnClickListener(v -> toggleCapability("reminder"));
        btnOptNotes.setOnClickListener(v -> toggleCapability("notes"));
        btnOptQuantity.setOnClickListener(v -> toggleCapability("quantity"));
        btnOptDate.setOnClickListener(v -> toggleCapability("date"));
        btnOptAttachments.setOnClickListener(v -> toggleCapability("attachments"));
    }

    private void toggleCapability(String type) {
        switch (type) {
            case "checkbox": hasCheckbox = !hasCheckbox; updateButtonState(btnOptCheckbox, hasCheckbox); break;
            case "reminder": hasReminder = !hasReminder; updateButtonState(btnOptReminder, hasReminder); break;
            case "notes": hasNotes = !hasNotes; updateButtonState(btnOptNotes, hasNotes); break;
            case "quantity": hasQuantity = !hasQuantity; updateButtonState(btnOptQuantity, hasQuantity); break;
            case "date": hasDate = !hasDate; updateButtonState(btnOptDate, hasDate); break;
            case "attachments": hasAttachments = !hasAttachments; updateButtonState(btnOptAttachments, hasAttachments); break;
        }
    }

    private void updateButtonState(View view, boolean active) {
        view.setAlpha(active ? 1.0f : 0.5f);
        view.setScaleX(active ? 1.05f : 1.0f);
        view.setScaleY(active ? 1.05f : 1.0f);
    }

    private void setupCharacterCounters() {
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvNameCount.setText(getString(R.string.char_count_30, s.length()));
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        etDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvDescCount.setText(getString(R.string.char_count_80, s.length()));
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupIconPicker(View view) {
        LinearLayout iconPicker = view.findViewById(R.id.iconPickerLayout);
        for (int iconRes : availableIcons) {
            ImageView iv = new ImageView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(100, 100);
            lp.setMargins(0, 0, 24, 0);
            iv.setLayoutParams(lp);
            iv.setPadding(20, 20, 20, 20);
            iv.setImageResource(iconRes);
            iv.setBackgroundResource(R.drawable.bg_circle_light_gray);
            iv.setOnClickListener(v -> {
                selectedIconRes = iconRes;
                ivSelectedIcon.setImageResource(iconRes);
                ivSelectedIcon.setImageTintList(ColorStateList.valueOf(selectedColor));
                customImageUri = null;
                ivCustomPhoto.setVisibility(View.GONE);
            });
            iconPicker.addView(iv);
        }
    }

    private void setupColorPicker(View view) {
        LinearLayout colorPicker = view.findViewById(R.id.colorPickerLayout);
        for (String colorStr : availableColors) {
            int color = Color.parseColor(colorStr);
            View colorDot = new View(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(80, 80);
            lp.setMargins(0, 0, 24, 0);
            colorDot.setLayoutParams(lp);
            
            android.graphics.drawable.GradientDrawable dot = new android.graphics.drawable.GradientDrawable();
            dot.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            dot.setColor(color);
            colorDot.setBackground(dot);
            
            colorDot.setOnClickListener(v -> {
                selectedColor = color;
                ivSelectedIcon.setImageTintList(ColorStateList.valueOf(selectedColor));
            });
            colorPicker.addView(colorDot);
        }
    }

    private void setupAdvancedToggle(View view) {
        view.findViewById(R.id.btnAdvancedOptions).setOnClickListener(v -> {
            if (layoutAdvanced.getVisibility() == View.VISIBLE) {
                layoutAdvanced.setVisibility(View.GONE);
                ivAdvancedChevron.setRotation(90);
            } else {
                layoutAdvanced.setVisibility(View.VISIBLE);
                ivAdvancedChevron.setRotation(270);
            }
        });
    }

    private void populateEditingData(View view) {
        etName.setText(editingSpace.getName());
        etDesc.setText(editingSpace.getDescription());
        selectedIconRes = editingSpace.getIconResId();
        selectedColor = editingSpace.getColorCode();
        privacyStatus = editingSpace.getPrivacyStatus();
        customImageUri = editingSpace.getImageUri();

        hasCheckbox = editingSpace.isHasCheckbox();
        hasReminder = editingSpace.isHasReminder();
        hasNotes = editingSpace.isHasNotes();
        hasQuantity = editingSpace.isHasQuantity();
        hasDate = editingSpace.isHasDate();
        hasImage = editingSpace.isHasImage();
        hasAttachments = editingSpace.isHasAttachments();
        
        updateButtonState(btnOptCheckbox, hasCheckbox);
        updateButtonState(btnOptReminder, hasReminder);
        updateButtonState(btnOptNotes, hasNotes);
        updateButtonState(btnOptQuantity, hasQuantity);
        updateButtonState(btnOptDate, hasDate);
        updateButtonState(btnOptAttachments, hasAttachments);
        
        int duration = editingSpace.getAutoRemoveDuration();
        if (duration == 0) spinnerAutoRemove.setSelection(0);
        else if (duration == 1) spinnerAutoRemove.setSelection(1);
        else if (duration == 7) spinnerAutoRemove.setSelection(2);

        ivSelectedIcon.setImageResource(selectedIconRes);
        if (selectedColor != 0) {
            ivSelectedIcon.setImageTintList(ColorStateList.valueOf(selectedColor));
        }
        
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvBtnLabel = view.findViewById(R.id.tvCreateButtonLabel);
        if (tvTitle != null) tvTitle.setText("Update Space");
        if (tvBtnLabel != null) tvBtnLabel.setText("Update Space");
    }

    private void validateAndSave() {
        String name = etName.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a space name", Toast.LENGTH_SHORT).show();
            return;
        }
        saveSpace(name, desc);
    }

    private void saveSpace(String name, String desc) {
        int autoRemoveDuration = 0;
        int selection = spinnerAutoRemove.getSelectedItemPosition();
        if (selection == 1) autoRemoveDuration = 1;
        else if (selection == 2) autoRemoveDuration = 7;

        int finalAutoRemoveDuration = autoRemoveDuration;
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            if (editingSpace == null) {
                CustomSpace space = new CustomSpace(name, selectedIconRes, customImageUri);
                space.setDescription(desc);
                space.setColorCode(selectedColor);
                space.setPrivacyStatus(privacyStatus);
                space.setHasCheckbox(hasCheckbox);
                space.setHasReminder(hasReminder);
                space.setHasNotes(hasNotes);
                space.setHasQuantity(hasQuantity);
                space.setHasDate(hasDate);
                space.setHasImage(hasImage);
                space.setHasAttachments(hasAttachments);
                space.setAutoRemoveDuration(finalAutoRemoveDuration);
                db.customSpaceDao().insertSpace(space);
            } else {
                editingSpace.setName(name);
                editingSpace.setDescription(desc);
                editingSpace.setIconResId(selectedIconRes);
                editingSpace.setColorCode(selectedColor);
                editingSpace.setPrivacyStatus(privacyStatus);
                editingSpace.setImageUri(customImageUri);
                editingSpace.setHasCheckbox(hasCheckbox);
                editingSpace.setHasReminder(hasReminder);
                editingSpace.setHasNotes(hasNotes);
                editingSpace.setHasQuantity(hasQuantity);
                editingSpace.setHasDate(hasDate);
                editingSpace.setHasImage(hasImage);
                editingSpace.setHasAttachments(hasAttachments);
                editingSpace.setAutoRemoveDuration(finalAutoRemoveDuration);
                db.customSpaceDao().updateSpace(editingSpace);
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> getParentFragmentManager().popBackStack());
            }
        });
    }
}
