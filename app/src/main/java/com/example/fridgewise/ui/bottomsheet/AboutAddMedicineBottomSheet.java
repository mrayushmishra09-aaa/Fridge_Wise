package com.example.fridgewise.ui.bottomsheet;

import com.example.fridgewise.R;
import com.example.fridgewise.data.*;
import com.example.fridgewise.model.*;
import com.example.fridgewise.adapter.*;
import com.example.fridgewise.util.*;
import com.example.fridgewise.ui.viewmodel.*;

import com.example.fridgewise.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AboutAddMedicineBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_about_add_medicine, container, false);

        Button btnGotIt = view.findViewById(R.id.btnGotIt);
        View btnClose = view.findViewById(R.id.btnClose);

        btnGotIt.setOnClickListener(v -> dismiss());
        btnClose.setOnClickListener(v -> dismiss());

        return view;
    }
}
