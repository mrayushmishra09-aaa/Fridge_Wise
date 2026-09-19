package com.example.fridgewise.ui.fragments;

import com.example.fridgewise.R;
import com.example.fridgewise.data.*;
import com.example.fridgewise.model.*;
import com.example.fridgewise.adapter.*;
import com.example.fridgewise.util.*;
import com.example.fridgewise.ui.viewmodel.*;
import com.example.fridgewise.ui.activities.*;
import com.example.fridgewise.ui.bottomsheet.*;

import com.example.fridgewise.R;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.Manifest;

import java.io.Serializable;

public class AddDocumentFragment extends Fragment {

    private String selectedImageUri = "";
    private String selectedMimeType = "";
    private ImageView ivDocPreview;
    private ActivityResultLauncher<String> filePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private DocumentItem editingDocument = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize the File Picker
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        saveAndShowFile(uri);
                    }
                }
        );

        // Initialize the Camera Launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        if (bitmap != null) {
                            String path = saveBitmapToInternalStorage(bitmap);
                            if (!path.isEmpty()) {
                                selectedImageUri = path;
                                if (ivDocPreview != null) {
                                    ivDocPreview.setImageBitmap(bitmap);
                                    ivDocPreview.setPadding(0, 0, 0, 0);
                                    ivDocPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                }
                            }
                        }
                    }
                }
        );

        // Initialize the Camera Permission Launcher
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_document, container, false);

        // --- Back Button ---
        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // --- View Initializations ---
        ivDocPreview = view.findViewById(R.id.ivDocPreview);
        EditText etDocName = view.findViewById(R.id.etDocName);
        AutoCompleteTextView actvCategory = view.findViewById(R.id.actvCategory);
        Button btnSaveDoc = view.findViewById(R.id.btnSaveDoc);
        TextView tvTitle = view.findViewById(R.id.tvAddDocTitle);

        // --- Category Dropdown ---
        String[] categories = {"Medical", "Personal", "Home", "Finance", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, categories);
        actvCategory.setAdapter(adapter);

        // --- Check for Edit Mode ---
        if (getArguments() != null && getArguments().containsKey("document")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                editingDocument = getArguments().getSerializable("document", DocumentItem.class);
            } else {
                editingDocument = (DocumentItem) getArguments().getSerializable("document");
            }
            
            if (editingDocument != null) {
                if (tvTitle != null) tvTitle.setText("Edit Document");
                etDocName.setText(editingDocument.getName());
                actvCategory.setText(editingDocument.getCategory(), false);
                selectedImageUri = editingDocument.getImagePath();
                selectedMimeType = editingDocument.getMimeType();
                
                if (selectedImageUri != null && !selectedImageUri.isEmpty()) {
                    updatePreview();
                }
                btnSaveDoc.setText("Update Document");
            }
        }

        // --- Photo/File Capture Click ---
        view.findViewById(R.id.cardCapture).setOnClickListener(v -> {
            if (getContext() == null) return;
            String[] options = {"Take Photo", "Choose File (Images, PDF, etc.)"};
            new AlertDialog.Builder(getContext())
                    .setTitle("Add Document")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                openCamera();
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                            }
                        } else {
                            // Open File Picker
                            filePickerLauncher.launch("*/*");
                        }
                    }).show();
        });

        // --- Save Button ---
        btnSaveDoc.setOnClickListener(v -> {
            String name = etDocName.getText().toString().trim();
            String category = actvCategory.getText().toString().trim();

            if (name.isEmpty() || category.isEmpty()) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Please fill name and category", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            Context context = getContext();
            if (context == null) return;

            new Thread(() -> {
                // selectedImageUri is already expected to be a local path if it was just picked.
                // If it's still a content URI (e.g. from older data in edit mode), we copy it now.
                if (selectedImageUri != null && selectedImageUri.startsWith("content://")) {
                    selectedImageUri = FileUtil.saveToInternalStorage(context, Uri.parse(selectedImageUri));
                }

                if (editingDocument == null) {
                    // INSERT new document
                    DocumentItem newDoc = new DocumentItem(name, category, selectedImageUri);
                    newDoc.setMimeType(selectedMimeType);
                    AppDatabase.getInstance(getContext()).documentDao().insert(newDoc);
                } else {
                    // UPDATE existing document
                    // Delete old local image if it has been changed
                    String oldPath = editingDocument.getImagePath();
                    if (oldPath != null && !oldPath.equals(selectedImageUri) && oldPath.startsWith("file://")) {
                        deleteLocalFile(oldPath);
                    }

                    editingDocument.setName(name);
                    editingDocument.setCategory(category);
                    editingDocument.setImagePath(selectedImageUri);
                    editingDocument.setMimeType(selectedMimeType);
                    AppDatabase.getInstance(getContext()).documentDao().update(editingDocument);
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), editingDocument == null ? "Document saved" : "Document updated", Toast.LENGTH_SHORT).show();
                        }
                        getParentFragmentManager().popBackStack();
                    });
                }
            }).start();
        });

        return view;
    }

    private void saveAndShowFile(Uri uri) {
        Context context = getContext();
        if (context != null) {
            new Thread(() -> {
                String mimeType = FileUtil.getMimeType(context, uri);
                String localPath = FileUtil.saveToInternalStorage(context, uri);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (!localPath.isEmpty()) {
                            selectedImageUri = localPath;
                            selectedMimeType = mimeType;
                            updatePreview();
                        } else {
                            Toast.makeText(getContext(), "Error saving file locally", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        }
    }

    private void updatePreview() {
        if (ivDocPreview == null) return;
        
        if (selectedMimeType != null && selectedMimeType.startsWith("image/")) {
            try {
                ivDocPreview.setImageURI(Uri.parse(selectedImageUri));
                ivDocPreview.setPadding(0, 0, 0, 0);
                ivDocPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                ivDocPreview.setImageResource(R.drawable.round_camera_alt_24);
                ivDocPreview.setPadding(40, 40, 40, 40);
            }
        } else {
            // Show file icon
            ivDocPreview.setImageResource(FileUtil.getIconForMimeType(selectedMimeType));
            ivDocPreview.setPadding(60, 60, 60, 60);
            ivDocPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }

    private String saveBitmapToInternalStorage(Bitmap bitmap) {
        Context context = getContext();
        if (context == null) return "";
        
        File storageDir = context.getFilesDir();
        String fileName = "doc_" + UUID.randomUUID().toString() + ".jpg";
        File file = new File(storageDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            selectedMimeType = "image/jpeg";
            return Uri.fromFile(file).toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void deleteLocalFile(String path) {
        try {
            Uri uri = Uri.parse(path);
            File file = new File(uri.getPath());
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
