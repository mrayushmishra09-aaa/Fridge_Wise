package com.example.fridgewise;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.fridgewise.data.PreferenceManager;

public class FridgeWiseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        applyTheme();
    }

    public void applyTheme() {
        PreferenceManager pref = new PreferenceManager(this);
        int theme = pref.getAppTheme();
        switch (theme) {
            case 1: // Light
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2: // Dark
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default: // System
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
