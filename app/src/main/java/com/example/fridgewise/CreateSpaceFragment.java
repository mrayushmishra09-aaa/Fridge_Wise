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
    private android.widget.CheckBox cbProgressBar, cbTodoList, cbDocument, cbTracking;
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
        cbProgressBar = view.findViewById(R.id.cbProgressBar);
        cbTodoList = view.findViewById(R.id.cbTodoList);
        cbDocument = view.findViewById(R.id.cbDocument);
        cbTracking = view.findViewById(R.id.cbTracking);
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

        cbProgressBar.setChecked(editingSpace.isHasProgressBar());
        cbTodoList.setChecked(editingSpace.isHasTodoList());
        cbDocument.setChecked(editingSpace.isHasDocuments());
        cbTracking.setChecked(editingSpace.isHasTracking());

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
        boolean hasProgress = cbProgressBar.isChecked();
        boolean hasTodo = cbTodoList.isChecked();
        boolean hasDoc = cbDocument.isChecked();
        boolean hasTrack = cbTracking.isChecked();

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            if (editingSpace == null) {
                CustomSpace space = new CustomSpace(name, selectedIconRes, customImageUri);
                space.setDescription(desc);
                space.setColorCode(selectedColor);
                space.setPrivacyStatus(privacyStatus);
                space.setHasProgressBar(hasProgress);
                space.setHasTodoList(hasTodo);
                space.setHasDocuments(hasDoc);
                space.setHasTracking(hasTrack);
                db.customSpaceDao().insertSpace(space);
            } else {
                editingSpace.setName(name);
                editingSpace.setDescription(desc);
                editingSpace.setIconResId(selectedIconRes);
                editingSpace.setColorCode(selectedColor);
                editingSpace.setPrivacyStatus(privacyStatus);
                editingSpace.setImageUri(customImageUri);
                editingSpace.setHasProgressBar(hasProgress);
                editingSpace.setHasTodoList(hasTodo);
                editingSpace.setHasDocuments(hasDoc);
                editingSpace.setHasTracking(hasTrack);
                db.customSpaceDao().updateSpace(editingSpace);
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> getParentFragmentManager().popBackStack());
            }
        });
    }
}
