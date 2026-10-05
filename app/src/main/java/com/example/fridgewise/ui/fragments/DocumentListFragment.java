package com.example.fridgewise.ui.fragments;

import com.example.fridgewise.R;
import com.example.fridgewise.data.*;
import com.example.fridgewise.model.*;
import com.example.fridgewise.adapter.*;
import com.example.fridgewise.util.*;
import com.example.fridgewise.ui.viewmodel.*;
import com.example.fridgewise.ui.activities.*;
import com.example.fridgewise.ui.bottomsheet.*;

import android.content.Context;
import android.os.Bundle;
import android.net.Uri;
import com.google.android.material.chip.ChipGroup;
import java.util.Locale;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

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
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.GridLayoutManager;

public class DocumentListFragment extends Fragment {

    private DocumentAdapter adapter;
    private TextView tvDocCount, tvSelectionCount;
    private View llEmptyState, headerLayout;
    private Toolbar selectionToolbar;
    private CheckBox cbSelectAll;
    private RecyclerView rvDocuments;
    private FloatingActionButton fab;
    private List<DocumentItem> currentDocuments = new ArrayList<>();
    private List<DocumentItem> allLoadedDocuments = new ArrayList<>();
    private ActivityResultLauncher<String> multiFilePickerLauncher;
    private String activeFilterKeyword = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        multiFilePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(),
            uris -> {
                if (uris != null && !uris.isEmpty()) {
                    importMultipleFiles(uris);
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_document_list, container, false);

        // --- Back Button ---
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // --- View Toggle Button (List <-> 3-Column Compact Square Grid) ---
        ImageView btnToggleView = view.findViewById(R.id.btnToggleView);
        if (btnToggleView != null) {
            btnToggleView.setOnClickListener(v -> {
                boolean newGridState = !adapter.isGrid();
                adapter.setGrid(newGridState);
                if (newGridState) {
                    rvDocuments.setLayoutManager(new GridLayoutManager(requireContext(), 3));
                    btnToggleView.setImageResource(R.drawable.ic_view_list);
                } else {
                    rvDocuments.setLayoutManager(new LinearLayoutManager(requireContext()));
                    btnToggleView.setImageResource(R.drawable.ic_view_grid);
                }
            });
        }

        // --- Quick Category Filter Chips (Including Appointments & Workouts) ---
        ChipGroup cgCategories = view.findViewById(R.id.cgDocCategories);
        if (cgCategories != null) {
            cgCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty() || checkedIds.contains(R.id.chipFilterAll)) {
                    filterDocuments(null);
                } else if (checkedIds.contains(R.id.chipFilterAppointments)) {
                    filterDocuments("Appointment");
                } else if (checkedIds.contains(R.id.chipFilterWorkouts)) {
                    filterDocuments("Workout");
                } else if (checkedIds.contains(R.id.chipFilterMedical)) {
                    filterDocuments("Medical");
                } else if (checkedIds.contains(R.id.chipFilterFinance)) {
                    filterDocuments("Finance");
                }
            });
        }

        // --- FAB Action Sheet (Quick Batch Import vs Detailed Form) ---
        fab = view.findViewById(R.id.fabAddDoc);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                String[] options = {"⚡ Quick Import Multiple Files / Photos", "📷 Take Photo / Detailed Form"};
                new AlertDialog.Builder(requireContext())
                        .setTitle("Add Document")
                        .setItems(options, (dialog, which) -> {
                            if (which == 0) {
                                multiFilePickerLauncher.launch("*/*");
                            } else {
                                Navigation.findNavController(v).navigate(R.id.addDocumentFragment);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        // Initialize UI Elements
        rvDocuments = view.findViewById(R.id.rvDocuments);
        tvDocCount = view.findViewById(R.id.tvDocCount);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        selectionToolbar = view.findViewById(R.id.selectionToolbar);
        headerLayout = view.findViewById(R.id.headerLayout);
        tvSelectionCount = view.findViewById(R.id.tvSelectionCount);
        cbSelectAll = view.findViewById(R.id.cbSelectAll);

        // --- Selection Toolbar Actions ---
        if (selectionToolbar != null) {
            selectionToolbar.setNavigationOnClickListener(v -> exitSelectionMode());
            selectionToolbar.inflateMenu(R.menu.menu_bulk_selection);
            selectionToolbar.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_share) {
                    bulkShare();
                    return true;
                } else if (itemId == R.id.action_delete) {
                    bulkDelete();
                    return true;
                }
                return false;
            });
        }

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

            new ItemTouchHelper(new SwipeToDeleteCallback(requireContext()) {
                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && currentDocuments != null && position < currentDocuments.size()) {
                        DocumentItem itemToDelete = currentDocuments.get(position);
                        
                        // Delete with standard native confirmation dialog
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Delete Document")
                                .setMessage("Are you sure you want to delete this document? This action cannot be undone.")
                                .setPositiveButton("Delete Forever", (dialog, which) -> {
                                    new Thread(() -> {
                                        deleteDocumentFiles(itemToDelete);
                                        AppDatabase.getInstance(requireContext()).documentDao().delete(itemToDelete);
                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(() -> {
                                                Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show();
                                                loadDocuments();
                                            });
                                        }
                                    }).start();
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> {
                                    adapter.notifyItemChanged(position);
                                })
                                .setOnCancelListener(dialog -> {
                                    adapter.notifyItemChanged(position);
                                })
                                .show();
                    }
                }
            }).attachToRecyclerView(rvDocuments);
        }

        loadDocuments();
        return view;
    }

    private void loadDocuments() {
        new Thread(() -> {
            List<DocumentItem> documents = AppDatabase.getInstance(requireContext()).documentDao().getAllDocuments();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    allLoadedDocuments = documents != null ? documents : new ArrayList<>();
                    filterDocuments(activeFilterKeyword);
                });
            }
        }).start();
    }

    private void filterDocuments(String keyword) {
        this.activeFilterKeyword = keyword;
        if (allLoadedDocuments == null) return;

        if (keyword == null || keyword.trim().isEmpty()) {
            currentDocuments = new ArrayList<>(allLoadedDocuments);
        } else {
            List<DocumentItem> filtered = new ArrayList<>();
            String lowerKw = keyword.toLowerCase(Locale.getDefault());
            for (DocumentItem doc : allLoadedDocuments) {
                String cat = doc.getCategory() != null ? doc.getCategory().toLowerCase(Locale.getDefault()) : "";
                String name = doc.getName() != null ? doc.getName().toLowerCase(Locale.getDefault()) : "";
                if (cat.contains(lowerKw) || name.contains(lowerKw)) {
                    filtered.add(doc);
                }
            }
            currentDocuments = filtered;
        }

        if (adapter != null) adapter.setDocs(currentDocuments);
        if (tvDocCount != null) {
            String countText = getResources().getQuantityString(R.plurals.documents_count, currentDocuments.size(), currentDocuments.size());
            tvDocCount.setText(countText);
        }
        if (llEmptyState != null && rvDocuments != null) {
            if (currentDocuments.isEmpty()) {
                llEmptyState.setVisibility(View.VISIBLE);
                rvDocuments.setVisibility(View.GONE);
            } else {
                llEmptyState.setVisibility(View.GONE);
                rvDocuments.setVisibility(View.VISIBLE);
            }
        }
    }

    private void enterSelectionMode() {
        if (selectionToolbar != null) selectionToolbar.setVisibility(View.VISIBLE);
        if (headerLayout != null) headerLayout.setVisibility(View.GONE);
        if (fab != null) fab.hide();

        // Hide main bottom navigation
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.bottomNavigationView);
            if (nav != null) nav.setVisibility(View.GONE);
        }
    }

    private void exitSelectionMode() {
        if (selectionToolbar != null) selectionToolbar.setVisibility(View.GONE);
        if (headerLayout != null) headerLayout.setVisibility(View.VISIBLE);
        if (fab != null) fab.show();
        if (cbSelectAll != null) cbSelectAll.setChecked(false);

        // Show main bottom navigation
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.bottomNavigationView);
            if (nav != null) nav.setVisibility(View.VISIBLE);
        }
        if (adapter != null) adapter.clearSelection();
    }

    private void bulkDelete() {
        List<DocumentItem> selectedItems = adapter.getSelectedItems();
        if (selectedItems.isEmpty()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete " + selectedItems.size() + " items?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete Forever", (dialog, which) -> {
                    new Thread(() -> {
                        for (DocumentItem item : selectedItems) {
                            deleteDocumentFiles(item);
                            AppDatabase.getInstance(requireContext()).documentDao().delete(item);
                        }
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), selectedItems.size() + " documents deleted", Toast.LENGTH_SHORT).show();
                                exitSelectionMode();
                                loadDocuments();
                            });
                        }
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void bulkShare() {
        List<DocumentItem> selectedItems = adapter.getSelectedItems();
        if (selectedItems.isEmpty()) return;

        ArrayList<Uri> imageUris = new ArrayList<>();
        for (DocumentItem item : selectedItems) {
            String path = item.getImagePath();
            if (path != null && !path.isEmpty()) {
                try {
                    File file;
                    if (path.startsWith("file://")) {
                        file = new File(Uri.parse(path).getPath());
                    } else {
                        file = new File(path);
                    }
                    
                    if (file.exists()) {
                        Uri contentUri = FileProvider.getUriForFile(requireContext(), "com.example.fridgewise.fileprovider", file);
                        imageUris.add(contentUri);
                    }
                } catch (Exception ignored) {}
            }
        }

        if (imageUris.isEmpty()) {
            Toast.makeText(getContext(), "No valid images to share", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent();
        if (imageUris.size() == 1) {
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUris.get(0));
        } else {
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        }
        
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share documents via"));
    }

    private void downloadDocument(DocumentItem document) {
        String imagePath = document.getImagePath();
        String mimeType = document.getMimeType();
        if (imagePath == null || imagePath.isEmpty()) return;

        new Thread(() -> {
            try {
                Uri sourceUri = Uri.parse(imagePath);
                String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                if (ext == null) ext = "dat";
                
                String fileName = "FridgeWise_" + System.currentTimeMillis() + "." + ext;
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                
                Uri externalUri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    externalUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                } else {
                    externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }

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
            } catch (Exception ignored) {}
        }).start();
    }

    private void deleteDocument(DocumentItem document) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Document")
                .setMessage("Are you sure you want to delete this document? This action cannot be undone.")
                .setPositiveButton("Delete Forever", (dialog, which) -> {
                    new Thread(() -> {
                        deleteDocumentFiles(document);
                        AppDatabase.getInstance(requireContext()).documentDao().delete(document);
                        if (getActivity() != null) getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show();
                            loadDocuments();
                        });
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
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

    private void importMultipleFiles(List<Uri> uris) {
        Context context = getContext();
        if (context == null || uris == null || uris.isEmpty()) return;

        Toast.makeText(context, "Importing " + uris.size() + " files...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            int importedCount = 0;
            AppDatabase db = AppDatabase.getInstance(context);

            for (Uri uri : uris) {
                try {
                    String localPath = FileUtil.saveToInternalStorage(context, uri);
                    String mimeType = context.getContentResolver().getType(uri);
                    if (mimeType == null) {
                        String ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                        if (ext != null) mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
                    }

                    String fileName = getFileNameFromUri(context, uri);
                    String category = "General";
                    if (mimeType != null) {
                        if (mimeType.startsWith("image/")) category = "Photos";
                        else if (mimeType.contains("pdf")) category = "PDF Docs";
                    }

                    DocumentItem doc = new DocumentItem(fileName, category, localPath);
                    doc.setMimeType(mimeType);
                    db.documentDao().insert(doc);
                    importedCount++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            final int count = importedCount;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Successfully imported " + count + " documents!", Toast.LENGTH_SHORT).show();
                    loadDocuments();
                });
            }
        }).start();
    }

    private String getFileNameFromUri(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            } catch (Exception ignored) {}
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) result = result.substring(cut + 1);
            }
        }
        return (result != null && !result.isEmpty()) ? result : "Imported_Doc_" + System.currentTimeMillis();
    }
}
