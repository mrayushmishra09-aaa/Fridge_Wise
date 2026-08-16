package com.example.fridgewise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AddDocumentFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_document, container, false);

        // --- Back Button ---
        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // --- Category Dropdown ---
        AutoCompleteTextView actvCategory = view.findViewById(R.id.actvCategory);
        String[] categories = {"Medical", "Personal", "Home", "Finance", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, categories);
        actvCategory.setAdapter(adapter);

        // --- Save Button (Backend logic for you to type) ---
        view.findViewById(R.id.btnSaveDoc).setOnClickListener(v -> {
            // TODO: Capture input, create DocumentItem, and save to Room
        });

        // --- Photo Capture (Advance logic for you to type) ---
        view.findViewById(R.id.cardCapture).setOnClickListener(v -> {
            // TODO: Launch Camera or Gallery
        });

        return view;
    }
}
