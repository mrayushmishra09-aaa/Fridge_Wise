package com.example.fridgewise.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
    private static final String TAG = "FileUtil";
    private static final String PROFILE_IMAGE_NAME = "profile_photo.jpg";

    public static String saveProfileImage(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File file = new File(context.getFilesDir(), PROFILE_IMAGE_NAME);
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            Log.e(TAG, "Error saving profile image", e);
            return null;
        }
    }
}
