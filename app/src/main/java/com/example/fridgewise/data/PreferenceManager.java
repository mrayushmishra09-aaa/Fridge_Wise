package com.example.fridgewise.data;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import android.util.Log;

public class PreferenceManager {
    private static final String TAG = "PreferenceManager";
    private static final String PREF_NAME = "FridgeWisePrefs_Secure";
    private static final String KEY_IS_FIRST_TIME = "isFirstTimeLaunch";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_IS_GUEST = "isGuest";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_DOB = "userDOB";
    private static final String KEY_USER_PASSWORD = "userPassword";
    private static final String KEY_PROFILE_IMAGE_URI = "profileImageUri";
    private static final String KEY_APP_THEME = "appTheme"; // 0: System, 1: Light, 2: Dark
    private static final String KEY_NOTIF_EXPIRY = "notifExpiry";
    private static final String KEY_NOTIF_GROCERY = "notifGrocery";
    private static final String KEY_SMART_FOLLOWUP = "smartFollowUp";
    private static final String KEY_ADVANCE_EXPIRY = "advanceExpiry";
    private static final String KEY_AUTO_DELETE_EXPIRED = "autoDeleteExpired";
    private static final String KEY_AUTO_CLEAR_COMPLETED = "autoClearCompleted";
    private static final String KEY_QUIET_HOURS_ENABLED = "quietHoursEnabled";
    private static final String KEY_QUIET_HOURS_START = "quietHoursStart"; // Hour of day 0-23
    private static final String KEY_QUIET_HOURS_END = "quietHoursEnd";   // Hour of day 0-23

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
        if (editor != null) {
            editor.putBoolean(KEY_IS_FIRST_TIME, isFirstTime);
            editor.apply();
        }
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(KEY_IS_FIRST_TIME, true);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        if (editor != null) {
            editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
            editor.apply();
        }
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setGuest(boolean isGuest) {
        if (editor != null) {
            editor.putBoolean(KEY_IS_GUEST, isGuest);
            editor.apply();
        }
    }

    public boolean isGuest() {
        return pref.getBoolean(KEY_IS_GUEST, false);
    }

    public void setUserName(String name) {
        if (editor != null) {
            editor.putString(KEY_USER_NAME, name);
            editor.apply();
        }
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "User");
    }

    public void setUserEmail(String email) {
        if (editor != null) {
            editor.putString(KEY_USER_EMAIL, email);
            editor.apply();
        }
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "user@email.com");
    }

    public void setUserDOB(String dob) {
        if (editor != null) {
            editor.putString(KEY_USER_DOB, dob);
            editor.apply();
        }
    }

    public String getUserDOB() {
        return pref.getString(KEY_USER_DOB, "Not Set");
    }

    public void setUserPassword(String password) {
        if (editor != null) {
            editor.putString(KEY_USER_PASSWORD, password);
            editor.apply();
        }
    }

    public String getUserPassword() {
        return pref.getString(KEY_USER_PASSWORD, "********");
    }

    public void setProfileImageUri(String uri) {
        if (editor != null) {
            editor.putString(KEY_PROFILE_IMAGE_URI, uri);
            editor.apply();
        }
    }

    public String getProfileImageUri() {
        return pref.getString(KEY_PROFILE_IMAGE_URI, null);
    }

    public void setSmartFollowUpEnabled(boolean enabled) {
        if (editor != null) {
            editor.putBoolean(KEY_SMART_FOLLOWUP, enabled);
            editor.apply();
        }
    }

    public boolean isSmartFollowUpEnabled() {
        return pref.getBoolean(KEY_SMART_FOLLOWUP, true);
    }

    public void setAdvanceExpiryEnabled(boolean enabled) {
        if (editor != null) {
            editor.putBoolean(KEY_ADVANCE_EXPIRY, enabled);
            editor.apply();
        }
    }

    public boolean isAdvanceExpiryEnabled() {
        return pref.getBoolean(KEY_ADVANCE_EXPIRY, true);
    }

    public void setAppTheme(int theme) {
        if (editor != null) {
            editor.putInt(KEY_APP_THEME, theme);
            editor.apply();
        }
    }

    public int getAppTheme() {
        return pref.getInt(KEY_APP_THEME, 0); // Default to System
    }

    public void setNotifExpiryEnabled(boolean enabled) {
        if (editor != null) {
            editor.putBoolean(KEY_NOTIF_EXPIRY, enabled);
            editor.apply();
        }
    }

    public boolean isNotifExpiryEnabled() {
        return pref.getBoolean(KEY_NOTIF_EXPIRY, true);
    }

    public void setNotifGroceryEnabled(boolean enabled) {
        if (editor != null) {
            editor.putBoolean(KEY_NOTIF_GROCERY, enabled);
            editor.apply();
        }
    }

    public boolean isNotifGroceryEnabled() {
        return pref.getBoolean(KEY_NOTIF_GROCERY, true);
    }

    public void setAutoDeleteExpired(boolean enabled) {
        if (editor != null) {
            editor.putBoolean(KEY_AUTO_DELETE_EXPIRED, enabled);
            editor.apply();
        }
    }

    public boolean isAutoDeleteExpired() {
        return pref.getBoolean(KEY_AUTO_DELETE_EXPIRED, false);
    }

    public void setAutoClearCompleted(boolean enabled) {
        if (editor != null) {
            editor.putBoolean(KEY_AUTO_CLEAR_COMPLETED, enabled);
            editor.apply();
        }
    }

    public boolean isAutoClearCompleted() {
        return pref.getBoolean(KEY_AUTO_CLEAR_COMPLETED, false);
    }

    public void setQuietHoursEnabled(boolean enabled) {
        if (editor != null) {
            editor.putBoolean(KEY_QUIET_HOURS_ENABLED, enabled);
            editor.apply();
        }
    }

    public boolean isQuietHoursEnabled() {
        return pref.getBoolean(KEY_QUIET_HOURS_ENABLED, true);
    }

    public void setQuietHoursStart(int hour) {
        if (editor != null) {
            editor.putInt(KEY_QUIET_HOURS_START, hour);
            editor.apply();
        }
    }

    public int getQuietHoursStart() {
        return pref.getInt(KEY_QUIET_HOURS_START, 22); // Default 10 PM
    }

    public void setQuietHoursEnd(int hour) {
        if (editor != null) {
            editor.putInt(KEY_QUIET_HOURS_END, hour);
            editor.apply();
        }
    }

    public int getQuietHoursEnd() {
        return pref.getInt(KEY_QUIET_HOURS_END, 7); // Default 7 AM
    }

    public void clearAll() {
        if (editor != null) {
            editor.clear();
            editor.apply();
        }
    }
}
