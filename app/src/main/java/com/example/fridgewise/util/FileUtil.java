package com.example.fridgewise.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.fridgewise.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class FileUtil {

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri != null && "content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            } catch (Exception ignored) {}
        }
        if (result == null && uri != null && uri.getPath() != null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    public static String getMimeType(Context context, Uri uri) {
        if (uri == null) return null;
        String mimeType = null;
        if ("content".equals(uri.getScheme())) {
            mimeType = context.getContentResolver().getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static String saveToInternalStorage(Context context, Uri uri) {
        return saveFile(context, uri, "doc_");
    }

    public static String saveProfileImage(Context context, Uri uri) {
        return saveFile(context, uri, "profile_");
    }

    private static String saveFile(Context context, Uri uri, String prefix) {
        if (uri == null || context == null) return "";
        try {
            String mimeType = getMimeType(context, uri);
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extension == null) extension = "dat";

            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return "";

            File storageDir = context.getFilesDir();
            String fileName = prefix + UUID.randomUUID().toString() + "." + extension;
            File file = new File(storageDir, fileName);

            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[8 * 1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            }
            inputStream.close();

            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getIconForMimeType(String mimeType) {
        if (mimeType == null) return R.drawable.ic_box;
        if (mimeType.startsWith("image/")) return R.drawable.round_camera_alt_24;
        if (mimeType.equals("application/pdf")) return R.drawable.v02_img_icons_doccc;
        if (mimeType.contains("word") || mimeType.contains("officedocument.wordprocessingml")) return R.drawable.v02_img_icons_doccc;
        return R.drawable.ic_box;
    }

    public static void openFile(Context context, String path, String mimeType) {
        try {
            Uri uri = Uri.parse(path);
            File file = new File(uri.getPath());
            if (!file.exists()) return;

            Uri contentUri = FileProvider.getUriForFile(context, "com.example.fridgewise.fileprovider", file);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            context.startActivity(Intent.createChooser(intent, "Open file with"));
        } catch (Exception e) {
            Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show();
        }
    }
}
