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
import java.util.List;

public class DocumentListFragment extends Fragment {

    private DocumentAdapter adapter;
    private TextView tvDocCount;

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
        RecyclerView rvDocuments = view.findViewById(R.id.rvDocuments);
        tvDocCount = view.findViewById(R.id.tvDocCount);

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
