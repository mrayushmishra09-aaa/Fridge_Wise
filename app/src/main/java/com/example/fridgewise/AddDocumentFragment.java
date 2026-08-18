package com.example.fridgewise;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.Serializable;

public class AddDocumentFragment extends Fragment {

    private String selectedImageUri = "";
    private ImageView ivDocPreview;
    private ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncher;
    private DocumentItem editingDocument = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize the Image Picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        Context context = getContext();
                        if (context != null) {
                            // Copy the image to internal storage immediately to avoid SecurityException later
                            new Thread(() -> {
                                String localPath = saveImageToInternalStorage(context, uri);
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        if (!localPath.isEmpty()) {
                                            selectedImageUri = localPath;
                                            if (ivDocPreview != null) {
                                                ivDocPreview.setImageURI(Uri.parse(selectedImageUri));
                                                ivDocPreview.setPadding(0, 0, 0, 0);
                                                ivDocPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                            }
                                        } else {
                                            Toast.makeText(getContext(), "Error saving image locally", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                }
        );
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
                if (selectedImageUri != null && !selectedImageUri.isEmpty()) {
                    try {
                        ivDocPreview.setImageURI(Uri.parse(selectedImageUri));
                        ivDocPreview.setPadding(0, 0, 0, 0);
                        ivDocPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    } catch (Exception e) {
                        // Fallback if permission expired or URI invalid
                        ivDocPreview.setImageResource(R.drawable.round_camera_alt_24);
                        ivDocPreview.setPadding(40, 40, 40, 40);
                    }
                }
                btnSaveDoc.setText("Update Document");
            }
        }

        // --- Photo Capture Click ---
        view.findViewById(R.id.cardCapture).setOnClickListener(v -> imagePickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        // --- Save Button ---
        btnSaveDoc.setOnClickListener(v -> {
            String name = etDocName.getText().toString().trim();
            String category = actvCategory.getText().toString().trim();

            if (name.isEmpty() || category.isEmpty()) {
                Toast.makeText(getContext(), "Please fill name and category", Toast.LENGTH_SHORT).show();
                return;
            }

            Context context = getContext();
            if (context == null) return;

            new Thread(() -> {
                // selectedImageUri is already expected to be a local path if it was just picked.
                // If it's still a content URI (e.g. from older data in edit mode), we copy it now.
                if (selectedImageUri != null && selectedImageUri.startsWith("content://")) {
                    selectedImageUri = saveImageToInternalStorage(context, Uri.parse(selectedImageUri));
                }

                if (editingDocument == null) {
                    // INSERT new document
                    DocumentItem newDoc = new DocumentItem(name, category, selectedImageUri);
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
                    AppDatabase.getInstance(getContext()).documentDao().update(editingDocument);
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), editingDocument == null ? "Document saved" : "Document updated", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    });
                }
            }).start();
        });

        return view;
    }

    /**
     * Copies the image from the given URI to the app's internal storage.
     * This avoids SecurityExceptions when accessing the URI later after permissions expire.
     */
    private String saveImageToInternalStorage(Context context, Uri uri) {
        if (uri == null || context == null) return "";
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return "";

            File storageDir = context.getFilesDir();
            String fileName = "doc_" + UUID.randomUUID().toString() + ".jpg";
            File file = new File(storageDir, fileName);

            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return Uri.fromFile(file).toString();
        } catch (Exception e) {
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
