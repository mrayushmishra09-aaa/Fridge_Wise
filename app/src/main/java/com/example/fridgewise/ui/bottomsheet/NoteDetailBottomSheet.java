package com.example.fridgewise.ui.bottomsheet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fridgewise.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class NoteDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_NOTE = "arg_note";
    private static final String ARG_TITLE = "arg_title";

    public static NoteDetailBottomSheet newInstance(String title, String note) {
        NoteDetailBottomSheet fragment = new NoteDetailBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_NOTE, note);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_note_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tvNoteTitle);
        TextView tvNote = view.findViewById(R.id.tvFullNote);
        View btnCopy = view.findViewById(R.id.btnCopyNote);

        if (getArguments() != null) {
            tvTitle.setText(getArguments().getString(ARG_TITLE, "Note"));
            tvNote.setText(getArguments().getString(ARG_NOTE, ""));
        }

        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("FridgeWise Note", tvNote.getText());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), R.string.note_copied, Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());
    }
}
