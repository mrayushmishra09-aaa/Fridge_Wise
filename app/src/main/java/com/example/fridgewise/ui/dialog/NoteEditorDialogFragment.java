package com.example.fridgewise.ui.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fridgewise.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class NoteEditorDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_INITIAL_NOTE = "arg_initial_note";
    private static final String ARG_TITLE = "arg_title";

    public interface OnNoteSavedListener {
        void onNoteSaved(String noteText);
    }

    private OnNoteSavedListener listener;
    private TextInputEditText etNoteContent;
    private TextView tvCharCount;
    private ImageView btnExpandNote;
    private boolean isExpanded = false;

    public static NoteEditorDialogFragment newInstance(String title, String initialNote) {
        NoteEditorDialogFragment fragment = new NoteEditorDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_INITIAL_NOTE, initialNote);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnNoteSavedListener(OnNoteSavedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.FloatingBottomSheetDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_note_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        etNoteContent = view.findViewById(R.id.etNoteContent);
        tvCharCount = view.findViewById(R.id.tvCharCount);
        btnExpandNote = view.findViewById(R.id.btnExpandNote);
        ImageView btnClose = view.findViewById(R.id.btnCloseNote);
        MaterialButton btnClear = view.findViewById(R.id.btnClearNote);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveNote);

        if (getArguments() != null) {
            String title = getArguments().getString(ARG_TITLE, "Edit Note");
            String initialNote = getArguments().getString(ARG_INITIAL_NOTE, "");
            tvTitle.setText(title);
            etNoteContent.setText(initialNote);
            if (initialNote != null && !initialNote.isEmpty()) {
                etNoteContent.setSelection(initialNote.length());
            }
            updateCharCount(initialNote != null ? initialNote.length() : 0);
        }

        etNoteContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCharCount(s != null ? s.length() : 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnExpandNote.setOnClickListener(v -> toggleExpansion());

        btnClear.setOnClickListener(v -> {
            etNoteContent.setText("");
        });

        btnSave.setOnClickListener(v -> {
            String text = etNoteContent.getText() != null ? etNoteContent.getText().toString().trim() : "";
            if (listener != null) {
                listener.onNoteSaved(text);
            }
            dismiss();
        });

        btnClose.setOnClickListener(v -> dismiss());
    }

    private void updateCharCount(int length) {
        if (tvCharCount != null) {
            tvCharCount.setText(length + " chars");
        }
    }

    private void toggleExpansion() {
        isExpanded = !isExpanded;
        if (getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                if (isExpanded) {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    behavior.setSkipCollapsed(true);
                    etNoteContent.setMinLines(12);
                } else {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    etNoteContent.setMinLines(5);
                }
            }
        }
    }
}
