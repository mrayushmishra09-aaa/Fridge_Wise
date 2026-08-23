package com.example.fridgewise;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import android.util.Log;

public class PreferenceManager {
    private static final String TAG = "PreferenceManager";
    private static final String PREF_NAME = "FridgeWisePrefs_Secure";
    private static final String KEY_IS_FIRST_TIME = "isFirstTimeLaunch";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_AGE = "userAge";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            pref = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            editor = pref.edit();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences", e);
            // Fallback to standard prefs if encryption fails
            pref = context.getSharedPreferences("FridgeWisePrefs_Fallback", Context.MODE_PRIVATE);
            editor = pref.edit();
        }
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(KEY_IS_FIRST_TIME, isFirstTime);
        editor.apply();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(KEY_IS_FIRST_TIME, true);
    }

    public void setUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "User");
    }

    public void setUserAge(int age) {
        editor.putInt(KEY_USER_AGE, age);
        editor.apply();
    }

    public int getUserAge() {
        return pref.getInt(KEY_USER_AGE, 0);
    }

    public void clearAll() {
        editor.clear();
        editor.apply();
    }
}
