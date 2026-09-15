package com.example.fridgewise.ui.fragments;

import com.example.fridgewise.R;
import com.example.fridgewise.data.*;
import com.example.fridgewise.model.*;
import com.example.fridgewise.adapter.*;
import com.example.fridgewise.util.*;
import com.example.fridgewise.ui.viewmodel.*;
import com.example.fridgewise.ui.activities.*;
import com.example.fridgewise.ui.bottomsheet.*;

import android.os.Bundle;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.os.Build;
import android.os.Environment;
import android.view.animation.AccelerateDecelerateInterpolator;

public class DocumentListFragment extends Fragment {

    private DocumentAdapter adapter;
    private TextView tvDocCount, tvSelectionCount;
    private View llEmptyState, selectionToolbar, headerLayout, bottomSelectionBar;
    private CheckBox cbSelectAll;
    private RecyclerView rvDocuments;
    private FloatingActionButton fab;
    private List<DocumentItem> currentDocuments = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_document_list, container, false);

        // --- Back Button ---
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // --- FAB to Add Screen ---
        fab = view.findViewById(R.id.fabAddDoc);
        if (fab != null) fab.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.addDocumentFragment));

        // Initialize UI Elements
        rvDocuments = view.findViewById(R.id.rvDocuments);
        tvDocCount = view.findViewById(R.id.tvDocCount);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        selectionToolbar = view.findViewById(R.id.selectionToolbar);
        headerLayout = view.findViewById(R.id.headerLayout);
        tvSelectionCount = view.findViewById(R.id.tvSelectionCount);
        bottomSelectionBar = view.findViewById(R.id.bottomSelectionBar);
        cbSelectAll = view.findViewById(R.id.cbSelectAll);

        // --- Selection Toolbar Actions ---
        view.findViewById(R.id.btnCloseSelection).setOnClickListener(v -> exitSelectionMode());
        view.findViewById(R.id.btnBulkDelete).setOnClickListener(v -> bulkDelete());
        view.findViewById(R.id.btnBulkDeleteTop).setOnClickListener(v -> bulkDelete());
        view.findViewById(R.id.btnBulkShare).setOnClickListener(v -> bulkShare());

        if (cbSelectAll != null) {
            cbSelectAll.setOnClickListener(v -> {
                if (cbSelectAll.isChecked()) {
                    adapter.selectAll();
                } else {
                    adapter.clearSelection();
                }
            });
        }

        // Setup the RecyclerView
        adapter = new DocumentAdapter(new DocumentAdapter.OnDocumentClickListener() {
            @Override
            public void onEditClick(DocumentItem document) {
                Bundle args = new Bundle();
                args.putSerializable("document", document);
                Navigation.findNavController(view).navigate(R.id.addDocumentFragment, args);
            }

            @Override
            public void onDeleteClick(DocumentItem document) {
                deleteDocument(document);
            }

            @Override
            public void onDownloadClick(DocumentItem document) {
                downloadDocument(document);
            }

            @Override
            public void onSelectionModeChanged(boolean isSelectionMode) {
                if (isSelectionMode) enterSelectionMode();
                else exitSelectionMode();
            }

            @Override
            public void onSelectionCountChanged(int count) {
                if (tvSelectionCount != null) {
                    if (count > 0 && count == currentDocuments.size()) {
                        tvSelectionCount.setText("All selected");
                        if (cbSelectAll != null) cbSelectAll.setChecked(true);
                    } else {
                        tvSelectionCount.setText(count + " selected");
                        if (cbSelectAll != null) cbSelectAll.setChecked(false);
                    }
                }
            }
        });

        if (rvDocuments != null) {
            rvDocuments.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvDocuments.setAdapter(adapter);
        }

        loadDocuments();
        return view;
    }

    private void loadDocuments() {
        new Thread(() -> {
            List<DocumentItem> documents = AppDatabase.getInstance(requireContext()).documentDao().getAllDocuments();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    currentDocuments = documents;
                    if (adapter != null) adapter.setDocs(documents);
                    if (tvDocCount != null) tvDocCount.setText(documents.size() + " Documents saved");
                    if (llEmptyState != null && rvDocuments != null) {
                        if (documents.isEmpty()) {
                            llEmptyState.setVisibility(View.VISIBLE);
                            rvDocuments.setVisibility(View.GONE);
                        } else {
                            llEmptyState.setVisibility(View.GONE);
                            rvDocuments.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }).start();
    }

    private void enterSelectionMode() {
        if (selectionToolbar != null) selectionToolbar.setVisibility(View.VISIBLE);
        if (headerLayout != null) headerLayout.setVisibility(View.GONE);
        if (fab != null) fab.hide();
        
        if (bottomSelectionBar != null) {
            bottomSelectionBar.setVisibility(View.VISIBLE);
            bottomSelectionBar.setTranslationY(300);
            bottomSelectionBar.animate()
                    .translationY(0)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void exitSelectionMode() {
        if (selectionToolbar != null) selectionToolbar.setVisibility(View.GONE);
        if (headerLayout != null) headerLayout.setVisibility(View.VISIBLE);
        if (fab != null) fab.show();
        if (cbSelectAll != null) cbSelectAll.setChecked(false);
        
        if (bottomSelectionBar != null) {
            bottomSelectionBar.animate()
                    .translationY(300)
                    .setDuration(300)
                    .withEndAction(() -> bottomSelectionBar.setVisibility(View.GONE))
                    .start();
        }
        if (adapter != null) adapter.clearSelection();
    }

    private void bulkDelete() {
        List<DocumentItem> selectedItems = adapter.getSelectedItems();
        if (selectedItems.isEmpty()) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm_delete, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                .setView(dialogView)
                .create();
        
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        tvTitle.setText("Delete " + selectedItems.size() + " items?");

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            new Thread(() -> {
                for (DocumentItem item : selectedItems) {
                    deleteDocumentFiles(item);
                    AppDatabase.getInstance(requireContext()).documentDao().delete(item);
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), selectedItems.size() + " documents deleted", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        exitSelectionMode();
                        loadDocuments();
                    });
                }
            }).start();
        });

        dialog.show();
    }

    private void bulkShare() {
        List<DocumentItem> selectedItems = adapter.getSelectedItems();
        if (selectedItems.isEmpty()) return;

        ArrayList<Uri> imageUris = new ArrayList<>();
        for (DocumentItem item : selectedItems) {
            String path = item.getImagePath();
            if (path != null && !path.isEmpty()) {
                try {
                    File file = new File(Uri.parse(path).getPath());
                    Uri contentUri = FileProvider.getUriForFile(requireContext(), "com.example.fridgewise.fileprovider", file);
                    imageUris.add(contentUri);
                } catch (Exception ignored) {}
            }
        }

        if (imageUris.isEmpty()) return;

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share documents via"));
    }

    private void downloadDocument(DocumentItem document) {
        String imagePath = document.getImagePath();
        if (imagePath == null || imagePath.isEmpty()) return;

        new Thread(() -> {
            try {
                Uri sourceUri = Uri.parse(imagePath);
                String fileName = "FridgeWise_" + System.currentTimeMillis() + ".jpg";
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    Uri externalUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                    Uri destinationUri = requireContext().getContentResolver().insert(externalUri, values);
                    if (destinationUri != null) {
                        try (InputStream is = requireContext().getContentResolver().openInputStream(sourceUri);
                             OutputStream os = requireContext().getContentResolver().openOutputStream(destinationUri)) {
                            byte[] buffer = new byte[8192];
                            int length;
                            while ((length = is.read(buffer)) > 0) os.write(buffer, 0, length);
                        }
                        if (getActivity() != null) getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Saved to Downloads", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    // Fallback for older APIs: Save to standard external storage or legacy MediaStore
                    Uri externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    Uri destinationUri = requireContext().getContentResolver().insert(externalUri, values);
                    if (destinationUri != null) {
                        try (InputStream is = requireContext().getContentResolver().openInputStream(sourceUri);
                             OutputStream os = requireContext().getContentResolver().openOutputStream(destinationUri)) {
                            byte[] buffer = new byte[8192];
                            int length;
                            while ((length = is.read(buffer)) > 0) os.write(buffer, 0, length);
                        }
                        if (getActivity() != null) getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Saved to Gallery", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void deleteDocument(DocumentItem document) {
        new Thread(() -> {
            deleteDocumentFiles(document);
            AppDatabase.getInstance(requireContext()).documentDao().delete(document);
            if (getActivity() != null) getActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show();
                loadDocuments();
            });
        }).start();
    }

    private void deleteDocumentFiles(DocumentItem document) {
        String imagePath = document.getImagePath();
        if (imagePath != null && imagePath.startsWith("file://")) {
            try {
                File file = new File(Uri.parse(imagePath).getPath());
                if (file.exists()) file.delete();
            } catch (Exception ignored) {}
        }
    }
}
