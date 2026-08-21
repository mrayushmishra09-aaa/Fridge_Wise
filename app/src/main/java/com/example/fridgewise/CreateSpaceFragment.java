package com.example.fridgewise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    private EditText etName;
    private ImageView ivSelectedIcon;
    private int selectedIconRes = R.drawable.v02_img_icons_household;
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

        etName = view.findViewById(R.id.etSpaceName);
        ivSelectedIcon = view.findViewById(R.id.ivSpaceIcon);
        LinearLayout iconPicker = view.findViewById(R.id.iconPickerLayout);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        Button btnCreate = view.findViewById(R.id.btnCreateSpace);

        if (editingSpace != null) {
            etName.setText(editingSpace.getName());
            selectedIconRes = editingSpace.getIconResId();
            ivSelectedIcon.setImageResource(selectedIconRes);
            customImageUri = editingSpace.getImageUri();
            if (tvTitle != null) tvTitle.setText("Update Space");
            if (btnCreate != null) btnCreate.setText("Update Space");
        }

        // Populate Icon Picker
        for (int iconRes : availableIcons) {
            ImageView iv = new ImageView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(120, 120);
            lp.setMargins(0, 0, 24, 0);
            iv.setLayoutParams(lp);
            iv.setPadding(20, 20, 20, 20);
            iv.setImageResource(iconRes);
            iv.setBackgroundResource(R.drawable.bg_circle_light_gray);
            iv.setOnClickListener(v -> {
                selectedIconRes = iconRes;
                ivSelectedIcon.setImageResource(iconRes);
                customImageUri = null; // Clear custom photo if icon selected
            });
            iconPicker.addView(iv);
        }

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        view.findViewById(R.id.btnCreateSpace).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a space name", Toast.LENGTH_SHORT).show();
                return;
            }
            saveSpace(name);
        });

        view.findViewById(R.id.btnChangePhoto).setOnClickListener(v -> {
            // Placeholder for photo selection
            Toast.makeText(getContext(), "Photo selection coming soon", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void saveSpace(String name) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            if (editingSpace == null) {
                // Create new
                CustomSpace space = new CustomSpace(name, selectedIconRes, customImageUri);
                db.customSpaceDao().insertSpace(space);
            } else {
                // Update existing
                editingSpace.setName(name);
                editingSpace.setIconResId(selectedIconRes);
                editingSpace.setImageUri(customImageUri);
                db.customSpaceDao().updateSpace(editingSpace);
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    getParentFragmentManager().popBackStack();
                });
            }
        });
    }
}
