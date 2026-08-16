package com.example.fridgewise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AboutMedicineBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_about_medicine, container, false);

        Button btnGotIt = view.findViewById(R.id.btnGotIt);
        View btnClose = view.findViewById(R.id.btnClose);

        btnGotIt.setOnClickListener(v -> dismiss());
        btnClose.setOnClickListener(v -> dismiss());

        return view;
    }
}
