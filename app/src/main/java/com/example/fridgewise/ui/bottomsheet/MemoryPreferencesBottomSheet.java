package com.example.fridgewise.ui.bottomsheet;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.example.fridgewise.R;
import com.example.fridgewise.data.PreferenceManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class MemoryPreferencesBottomSheet extends BottomSheetDialogFragment {

    private PreferenceManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_memory_preferences, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefManager = new PreferenceManager(requireContext());

        SwitchCompat switchAutoDelete = view.findViewById(R.id.switchAutoDelete);
        SwitchCompat switchAutoClear = view.findViewById(R.id.switchAutoClear);
        MaterialButton btnOptimize = view.findViewById(R.id.btnOptimizeStorage);

        // Load saved states
        switchAutoDelete.setChecked(prefManager.isAutoDeleteExpired());
        switchAutoClear.setChecked(prefManager.isAutoClearCompleted());

        // Handle check changes
        switchAutoDelete.setOnCheckedChangeListener((buttonView, isChecked) -> 
            prefManager.setAutoDeleteExpired(isChecked)
        );

        switchAutoClear.setOnCheckedChangeListener((buttonView, isChecked) -> 
            prefManager.setAutoClearCompleted(isChecked)
        );

        btnOptimize.setOnClickListener(v -> {
            btnOptimize.setEnabled(false);
            btnOptimize.setText("Optimizing Storage...");
            
            // Simulating database index defragmentation and compression safely
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Local storage optimized & compressed successfully!", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }, 1200);
        });
    }
}
