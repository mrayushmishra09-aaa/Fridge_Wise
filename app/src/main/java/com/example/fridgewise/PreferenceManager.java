package com.example.fridgewise;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "FridgeWisePrefs";
    private static final String KEY_IS_FIRST_TIME = "isFirstTimeLaunch";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_AGE = "userAge";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
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
}
