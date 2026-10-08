package com.example.fridgewise.ui.fragments;

import com.example.fridgewise.R;
import com.example.fridgewise.data.*;
import com.example.fridgewise.model.*;
import com.example.fridgewise.adapter.*;
import com.example.fridgewise.util.SecurityManager;
import com.example.fridgewise.ui.viewmodel.*;
import com.example.fridgewise.ui.activities.*;
import com.example.fridgewise.ui.bottomsheet.*;
import com.example.fridgewise.ui.dialog.*;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.widget.SwitchCompat;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.transition.TransitionManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.util.concurrent.Executors;

public class CreateSpaceFragment extends Fragment {

    private EditText etName;
    private TextView tvNameCount;
    private ImageView ivSelectedIcon, ivCustomPhoto, ivAdvancedChevron;
    private MaterialCardView layoutAdvanced;
    private View btnOptCheckbox, btnOptReminder, btnOptNotes, btnOptQuantity, btnOptDate, btnOptAttachments, btnOptStreak;
    private MaterialCardView cardOptCheckbox, cardOptReminder, cardOptNotes, cardOptQuantity, cardOptDate, cardOptAttachments, cardOptStreak;
    private boolean hasCheckbox, hasReminder, hasNotes, hasQuantity, hasDate, hasAttachments, isStreakEnabled = true;
    private Spinner spinnerAutoRemove;
    private SwitchCompat switchProtectedSpace, switchHiddenSpace;
    private TextInputLayout tilSpaceName;
    private int selectedIconRes = R.drawable.round_camera_alt_24;
    private int selectedColor = Color.parseColor("#2D6A4F"); // Default Green
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
        setupQuickTemplates(view);
        setupIconPicker(view);
        setupColorPicker(view);
        setupCharacterCounters();
        setupAdvancedToggle(view);
        setupProtectionSwitches();

        if (editingSpace != null) {
            populateEditingData(view);
        }

        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        view.findViewById(R.id.btnCreateSpace).setOnClickListener(v -> validateAndSave());

        return view;
    }

    private void initializeViews(View view) {
        etName = view.findViewById(R.id.etSpaceName);
        tvNameCount = view.findViewById(R.id.tvNameCount);
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
        btnOptStreak = view.findViewById(R.id.btnOptStreak);
        
        cardOptCheckbox = view.findViewById(R.id.cardOptCheckbox);
        cardOptReminder = view.findViewById(R.id.cardOptReminder);
        cardOptNotes = view.findViewById(R.id.cardOptNotes);
        cardOptQuantity = view.findViewById(R.id.cardOptQuantity);
        cardOptDate = view.findViewById(R.id.cardOptDate);
        cardOptAttachments = view.findViewById(R.id.cardOptAttachments);
        cardOptStreak = view.findViewById(R.id.cardOptStreak);

        switchProtectedSpace = view.findViewById(R.id.switchProtectedSpace);
        switchHiddenSpace = view.findViewById(R.id.switchHiddenSpace);
        
        tilSpaceName = view.findViewById(R.id.tilSpaceName);
        
        setupCapabilityButtons();
        
        spinnerAutoRemove = view.findViewById(R.id.spinnerAutoRemove);
    }

    private void setupProtectionSwitches() {
        if (switchProtectedSpace != null) {
            switchProtectedSpace.setOnClickListener(v -> {
                if (switchProtectedSpace.isChecked()) {
                    com.example.fridgewise.util.SecurityManager sec = com.example.fridgewise.util.SecurityManager.getInstance(requireContext());
                    if (!sec.isMasterLockEnabled()) {
                        switchProtectedSpace.setChecked(false);
                        SetupPinDialog dialog = SetupPinDialog.newInstance("Set Master PIN for Protection", new SetupPinDialog.SetupPinCallback() {
                            @Override
                            public void onPinCreated(String pin, String salt, String hash) {
                                sec.setupMasterPin(pin);
                                switchProtectedSpace.setChecked(true);
                                Toast.makeText(requireContext(), "Master PIN set up and space protected", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCanceled() {
                                switchProtectedSpace.setChecked(false);
                            }
                        });
                        dialog.show(getParentFragmentManager(), "setup_pin_create_space");
                    }
                }
            });
        }
    }

    private void setupCapabilityButtons() {
        if (btnOptCheckbox != null) btnOptCheckbox.setOnClickListener(v -> toggleCapability("checkbox"));
        if (btnOptReminder != null) btnOptReminder.setOnClickListener(v -> toggleCapability("reminder"));
        if (btnOptNotes != null) btnOptNotes.setOnClickListener(v -> toggleCapability("notes"));
        if (btnOptQuantity != null) btnOptQuantity.setOnClickListener(v -> toggleCapability("quantity"));
        if (btnOptDate != null) btnOptDate.setOnClickListener(v -> toggleCapability("date"));
        if (btnOptAttachments != null) btnOptAttachments.setOnClickListener(v -> toggleCapability("attachments"));
        if (btnOptStreak != null) btnOptStreak.setOnClickListener(v -> toggleCapability("streak"));
    }

    private void toggleCapability(String type) {
        switch (type) {
            case "checkbox": 
                hasCheckbox = !hasCheckbox; 
                if (hasCheckbox) {
                    hasAttachments = false;
                    updateButtonState(btnOptAttachments, false);
                }
                updateButtonState(btnOptCheckbox, hasCheckbox); 
                break;
            case "reminder": hasReminder = !hasReminder; updateButtonState(btnOptReminder, hasReminder); break;
            case "notes": hasNotes = !hasNotes; updateButtonState(btnOptNotes, hasNotes); break;
            case "quantity": hasQuantity = !hasQuantity; updateButtonState(btnOptQuantity, hasQuantity); break;
            case "date": hasDate = !hasDate; updateButtonState(btnOptDate, hasDate); break;
            case "attachments": 
                hasAttachments = !hasAttachments; 
                if (hasAttachments) {
                    hasCheckbox = false;
                    isStreakEnabled = false;
                    updateButtonState(btnOptCheckbox, false);
                    updateButtonState(btnOptStreak, false);
                }
                updateButtonState(btnOptAttachments, hasAttachments); 
                break;
            case "streak": 
                isStreakEnabled = !isStreakEnabled; 
                if (isStreakEnabled) {
                    hasCheckbox = true;
                    hasAttachments = false;
                    updateButtonState(btnOptCheckbox, true);
                    updateButtonState(btnOptAttachments, false);
                }
                updateButtonState(btnOptStreak, isStreakEnabled); 
                break;
        }
    }

    private void updateButtonState(View view, boolean active) {
        if (view == null) return;
        view.setAlpha(active ? 1.0f : 0.5f);
        view.animate().scaleX(active ? 1.05f : 1.0f).scaleY(active ? 1.05f : 1.0f).setDuration(200).start();
        
        MaterialCardView card = null;
        if (view.getId() == R.id.btnOptCheckbox) card = cardOptCheckbox;
        else if (view.getId() == R.id.btnOptReminder) card = cardOptReminder;
        else if (view.getId() == R.id.btnOptNotes) card = cardOptNotes;
        else if (view.getId() == R.id.btnOptQuantity) card = cardOptQuantity;
        else if (view.getId() == R.id.btnOptDate) card = cardOptDate;
        else if (view.getId() == R.id.btnOptAttachments) card = cardOptAttachments;
        else if (view.getId() == R.id.btnOptStreak) card = cardOptStreak;
        
        if (card != null) {
            card.setStrokeWidth(active ? 4 : 1);
            if (active && selectedColor != 0) {
                card.setStrokeColor(ColorStateList.valueOf(selectedColor));
            } else {
                card.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.divider_color)));
            }
        }
    }

    private void setupCharacterCounters() {
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                tvNameCount.setText(getString(R.string.char_count_30, length));
                if (length >= 30) {
                    tvNameCount.setTextColor(Color.RED);
                    tilSpaceName.setError("Limit reached");
                } else {
                    tvNameCount.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_light));
                    tilSpaceName.setError(null);
                }
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
            
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
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
            TransitionManager.beginDelayedTransition((ViewGroup) view);
            if (layoutAdvanced.getVisibility() == View.VISIBLE) {
                layoutAdvanced.setVisibility(View.GONE);
                ivAdvancedChevron.setRotation(0);
            } else {
                layoutAdvanced.setVisibility(View.VISIBLE);
                ivAdvancedChevron.setRotation(90);
            }
        });
    }

    private void populateEditingData(View view) {
        etName.setText(editingSpace.getName());
        selectedIconRes = editingSpace.getIconResId();
        selectedColor = editingSpace.getColorCode();
        customImageUri = editingSpace.getImageUri();

        hasCheckbox = editingSpace.isHasCheckbox();
        hasReminder = editingSpace.isHasReminder();
        hasNotes = editingSpace.isHasNotes();
        hasQuantity = editingSpace.isHasQuantity();
        hasDate = editingSpace.isHasDate();
        hasAttachments = editingSpace.isHasAttachments();
        isStreakEnabled = editingSpace.isStreakEnabled();

        if (switchProtectedSpace != null) switchProtectedSpace.setChecked(editingSpace.isProtected());
        if (switchHiddenSpace != null) switchHiddenSpace.setChecked(editingSpace.isHidden());
        
        updateButtonState(btnOptCheckbox, hasCheckbox);
        updateButtonState(btnOptReminder, hasReminder);
        updateButtonState(btnOptNotes, hasNotes);
        updateButtonState(btnOptQuantity, hasQuantity);
        updateButtonState(btnOptDate, hasDate);
        updateButtonState(btnOptAttachments, hasAttachments);
        updateButtonState(btnOptStreak, isStreakEnabled);
        
        int duration = editingSpace.getAutoRemoveDuration();
        if (duration == 0) spinnerAutoRemove.setSelection(0);
        else if (duration == 1) spinnerAutoRemove.setSelection(1);
        else if (duration == 7) spinnerAutoRemove.setSelection(2);

        ivSelectedIcon.setImageResource(selectedIconRes);
        if (selectedColor != 0) {
            ivSelectedIcon.setImageTintList(ColorStateList.valueOf(selectedColor));
        }
        
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        MaterialButton btnCreate = view.findViewById(R.id.btnCreateSpace);
        if (tvTitle != null) tvTitle.setText("Update Space");
        if (btnCreate != null) btnCreate.setText("Update Space");
    }

    private void validateAndSave() {
        String name = etName.getText().toString().trim();
        
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a space name", Toast.LENGTH_SHORT).show();
            return;
        }
        saveSpace(name);
    }

    private void saveSpace(String name) {
        int autoRemoveDuration = 0;
        int selection = spinnerAutoRemove.getSelectedItemPosition();
        if (selection == 1) autoRemoveDuration = 1;
        else if (selection == 2) autoRemoveDuration = 7;

        if (isStreakEnabled) {
            hasCheckbox = true;
        }

        boolean isProtected = switchProtectedSpace != null && switchProtectedSpace.isChecked();
        boolean isHidden = switchHiddenSpace != null && switchHiddenSpace.isChecked();

        int finalAutoRemoveDuration = autoRemoveDuration;
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            if (editingSpace == null) {
                CustomSpace space = new CustomSpace(name, selectedIconRes, customImageUri);
                space.setColorCode(selectedColor);
                space.setHasCheckbox(hasCheckbox);
                space.setHasReminder(hasReminder);
                space.setHasNotes(hasNotes);
                space.setHasQuantity(hasQuantity);
                space.setHasDate(hasDate);
                space.setHasAttachments(hasAttachments);
                space.setStreakEnabled(isStreakEnabled);
                space.setAutoRemoveDuration(finalAutoRemoveDuration);

                space.setProtected(isProtected);
                space.setHidden(isHidden);
                space.setProtectionType(isProtected ? "MASTER_PIN" : "NONE");

                db.customSpaceDao().insertSpace(space);
            } else {
                editingSpace.setName(name);
                editingSpace.setIconResId(selectedIconRes);
                editingSpace.setColorCode(selectedColor);
                editingSpace.setImageUri(customImageUri);
                editingSpace.setHasCheckbox(hasCheckbox);
                editingSpace.setHasReminder(hasReminder);
                editingSpace.setHasNotes(hasNotes);
                editingSpace.setHasQuantity(hasQuantity);
                editingSpace.setHasDate(hasDate);
                editingSpace.setHasAttachments(hasAttachments);
                editingSpace.setStreakEnabled(isStreakEnabled);
                editingSpace.setAutoRemoveDuration(finalAutoRemoveDuration);

                editingSpace.setProtected(isProtected);
                editingSpace.setHidden(isHidden);
                if (isProtected && "NONE".equals(editingSpace.getProtectionType())) {
                    editingSpace.setProtectionType("MASTER_PIN");
                } else if (!isProtected) {
                    editingSpace.setProtectionType("NONE");
                }

                db.customSpaceDao().updateSpace(editingSpace);
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (getView() != null) Navigation.findNavController(getView()).popBackStack();
                });
            }
        });
    }

    private void setupQuickTemplates(View view) {
        View chipStudy = view.findViewById(R.id.chipTemplateStudy);
        View chipDorm = view.findViewById(R.id.chipTemplateDorm);
        View chipHealth = view.findViewById(R.id.chipTemplateHealth);
        View chipTravel = view.findViewById(R.id.chipTemplateTravel);
        View chipProvisions = view.findViewById(R.id.chipTemplateProvisions);

        if (chipStudy != null) {
            chipStudy.setOnClickListener(v -> applyTemplate("Study & Exams", R.drawable.v02_img_icons_doccc, "#7B61FF", true, true, true, false, true, true, false));
        }
        if (chipDorm != null) {
            chipDorm.setOnClickListener(v -> applyTemplate("Dorm & Tech", R.drawable.v02_img_icons_household, "#4A90E2", false, false, true, true, true, true, false));
        }
        if (chipHealth != null) {
            chipHealth.setOnClickListener(v -> applyTemplate("Health & Habits", R.drawable.v02_img_icons_medicne, "#FF6B6B", true, true, true, false, false, false, true));
        }
        if (chipTravel != null) {
            chipTravel.setOnClickListener(v -> applyTemplate("Travel Packing", R.drawable.v02_img_icons_shopping, "#FFB347", true, false, true, true, false, false, false));
        }
        if (chipProvisions != null) {
            chipProvisions.setOnClickListener(v -> applyTemplate("Provisions", R.drawable.v02_img_icons_household, "#2D6A4F", false, true, true, true, true, false, false));
        }
    }

    private void applyTemplate(String name, int iconRes, String colorHex, boolean chk, boolean rem, boolean notes, boolean qty, boolean date, boolean attach, boolean streak) {
        etName.setText(name);
        selectedIconRes = iconRes;
        selectedColor = Color.parseColor(colorHex);
        if (ivSelectedIcon != null) {
            ivSelectedIcon.setImageResource(iconRes);
        }
        
        hasCheckbox = chk; updateButtonState(btnOptCheckbox, chk);
        hasReminder = rem; updateButtonState(btnOptReminder, rem);
        hasNotes = notes; updateButtonState(btnOptNotes, notes);
        hasQuantity = qty; updateButtonState(btnOptQuantity, qty);
        hasDate = date; updateButtonState(btnOptDate, date);
        hasAttachments = attach; updateButtonState(btnOptAttachments, attach);
        isStreakEnabled = streak; updateButtonState(btnOptStreak, streak);
        
        Toast.makeText(getContext(), name + " template applied!", Toast.LENGTH_SHORT).show();
    }
}
