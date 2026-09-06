package com.example.fridgewise.ui.activities;

import com.example.fridgewise.R;
import com.example.fridgewise.data.*;
import com.example.fridgewise.model.*;
import com.example.fridgewise.adapter.*;
import com.example.fridgewise.util.*;
import com.example.fridgewise.ui.viewmodel.*;

import com.example.fridgewise.R;

import com.example.fridgewise.R;
import com.example.fridgewise.ui.fragments.IntroFragment;
import com.example.fridgewise.ui.fragments.welcomeFragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.splah_001);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                PreferenceManager prefManager = new PreferenceManager(MainActivity2.this);
                Intent intent;
                if (prefManager.isFirstTimeLaunch()) {
                    intent = new Intent(MainActivity2.this, OnboardingActivity.class);
                } else {
                    intent = new Intent(MainActivity2.this, MainActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 2000); // 2000 milliseconds = 2 seconds delay

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
