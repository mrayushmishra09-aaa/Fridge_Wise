package com.example.fridgewise;

import android.os.Bundle;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.os.Build;
import android.os.Environment;

public class DocumentListFragment extends Fragment {

    private DocumentAdapter adapter;
    private TextView tvDocCount;
    private View llEmptyState;
    private RecyclerView rvDocuments;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_document_list, container, false);

        // --- Back Button ---
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // --- FAB to Add Screen ---
        FloatingActionButton fab = view.findViewById(R.id.fabAddDoc);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView2, new AddDocumentFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // Initialize UI Elements
        rvDocuments = view.findViewById(R.id.rvDocuments);
        tvDocCount = view.findViewById(R.id.tvDocCount);
        llEmptyState = view.findViewById(R.id.llEmptyState);

        // Setup the RecyclerView
        adapter = new DocumentAdapter(new DocumentAdapter.OnDocumentClickListener() {
            @Override
            public void onEditClick(DocumentItem document) {
                AddDocumentFragment fragment = new AddDocumentFragment();
                Bundle args = new Bundle();
                args.putSerializable("document", document);
                fragment.setArguments(args);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView2, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDeleteClick(DocumentItem document) {
                deleteDocument(document);
            }

            @Override
            public void onDownloadClick(DocumentItem document) {
                downloadDocument(document);
            }
        });

        if (rvDocuments != null) {
            rvDocuments.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvDocuments.setAdapter(adapter);
        }

        // Load data from database
        loadDocuments();

        return view;
    }

    private void loadDocuments() {
        new Thread(() -> {
            List<DocumentItem> documents = AppDatabase.getInstance(requireContext()).documentDao().getAllDocuments();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.setDocs(documents);
                    }
                    if (tvDocCount != null) {
                        tvDocCount.setText(getString(R.string.documents_saved_count, documents.size()));
                    }
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

    private void downloadDocument(DocumentItem document) {
        String imagePath = document.getImagePath();
        if (imagePath == null || imagePath.isEmpty()) {
            Toast.makeText(getContext(), "No image to download", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            boolean success = false;
            try {
                Uri sourceUri = Uri.parse(imagePath);
                String fileName = "FridgeWise_" + document.getName().replace(" ", "_") + ".jpg";

                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                
                Uri externalUri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    externalUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                } else {
                    // Fallback for older versions
                    externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }

                Uri destinationUri = requireContext().getContentResolver().insert(externalUri, values);

                if (destinationUri != null) {
                    try (InputStream is = requireContext().getContentResolver().openInputStream(sourceUri);
                         OutputStream os = requireContext().getContentResolver().openOutputStream(destinationUri)) {
                        
                        byte[] buffer = new byte[8192];
                        int length;
                        while ((length = is.read(buffer)) > 0) {
                            os.write(buffer, 0, length);
                        }
                        success = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            final boolean finalSuccess = success;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (finalSuccess) {
                        Toast.makeText(getContext(), "Document saved to Downloads", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to save document", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private void deleteDocument(DocumentItem document) {
        new Thread(() -> {
            // Delete local image file if it exists
            String imagePath = document.getImagePath();
            if (imagePath != null && imagePath.startsWith("file://")) {
                try {
                    File file = new File(Uri.parse(imagePath).getPath());
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            AppDatabase.getInstance(requireContext()).documentDao().delete(document);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Document deleted", Toast.LENGTH_SHORT).show();
                    loadDocuments();
                });
            }
        }).start();
    }
}
