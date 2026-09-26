package com.example.fridgewise.ui.activities;

import com.example.fridgewise.R;
import com.example.fridgewise.data.*;
import com.example.fridgewise.model.*;
import com.example.fridgewise.adapter.*;
import com.example.fridgewise.util.*;
import com.example.fridgewise.ui.viewmodel.*;

import com.example.fridgewise.ui.fragments.HomeFragment;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.example.fridgewise.util.IdentityNudgeWorker;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        PreferenceManager prefManager = new PreferenceManager(this);
        if (!prefManager.isLoggedIn() && (prefManager.isFirstTimeLaunch() || !prefManager.isGuest())) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView2);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // Ensure sub-destinations map to their parent bottom nav tab
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();
                int tabMenuId = -1;

                if (destId == R.id.nav_home || destId == R.id.globalSearchFragment) {
                    tabMenuId = R.id.nav_home;
                } else if (destId == R.id.nav_inventory || destId == R.id.addItemFragment) {
                    tabMenuId = R.id.nav_inventory;
                } else if (destId == R.id.nav_memory || destId == R.id.med_section
                        || destId == R.id.todoListFragment || destId == R.id.shoppingListFragment
                        || destId == R.id.documentListFragment || destId == R.id.createSpaceFragment
                        || destId == R.id.addSpaceItemFragment || destId == R.id.customSpaceInventoryFragment
                        || destId == R.id.medicineAddFragment || destId == R.id.addTodoFragment
                        || destId == R.id.addDocumentFragment) {
                    tabMenuId = R.id.nav_memory;
                } else if (destId == R.id.nav_streak) {
                    tabMenuId = R.id.nav_streak;
                } else if (destId == R.id.nav_pfp) {
                    tabMenuId = R.id.nav_pfp;
                }

                if (tabMenuId != -1) {
                    MenuItem item = bottomNavigationView.getMenu().findItem(tabMenuId);
                    if (item != null && !item.isChecked()) {
                        item.setChecked(true);
                    }
                }
            });

            // Handle tab reselection: reset to tab root if in sub-destination
            bottomNavigationView.setOnItemReselectedListener(item -> {
                int itemId = item.getItemId();
                int currentDestId = navController.getCurrentDestination() != null ? navController.getCurrentDestination().getId() : -1;

                if (itemId == R.id.nav_memory && currentDestId != R.id.nav_memory) {
                    navController.popBackStack(R.id.nav_memory, false);
                } else if (itemId == R.id.nav_home && currentDestId != R.id.nav_home) {
                     navController.popBackStack(R.id.nav_home, false);
                } else if (itemId == R.id.nav_inventory && currentDestId != R.id.nav_inventory) {
                    navController.popBackStack(R.id.nav_inventory, false);
                } else if (itemId == R.id.nav_streak && currentDestId != R.id.nav_streak) {
                    navController.popBackStack(R.id.nav_streak, false);
                } else if (itemId == R.id.nav_pfp && currentDestId != R.id.nav_pfp) {
                    navController.popBackStack(R.id.nav_pfp, false);
                }
            });
        }

        // Set default fragment on first launch
        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }

        // fixed extra spacing for bottom navigation
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    0
            );

            return insets;
        });

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }

        scheduleSmartCleanup();
        scheduleIdentityNudge();
    }

    private void scheduleIdentityNudge() {
        PeriodicWorkRequest nudgeRequest = new PeriodicWorkRequest.Builder(
                IdentityNudgeWorker.class, 2, TimeUnit.DAYS) // Nudge every 2 days
                .setInitialDelay(1, TimeUnit.DAYS) // Wait 1 day before first nudge
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "IdentityNudgeWork",
                ExistingPeriodicWorkPolicy.KEEP,
                nudgeRequest
        );
    }

    private void scheduleSmartCleanup() {
        PeriodicWorkRequest cleanupRequest = new PeriodicWorkRequest.Builder(
                CleanupWorker.class, 6, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "SmartCleanupWork",
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupRequest
        );
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        
        String target = intent.getStringExtra("target_fragment");
        
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView2);
        if (navHostFragment == null) return;
        NavController navController = navHostFragment.getNavController();

        if (target == null) return;

        switch (target) {
            case "MEDICINE":
                navController.navigate(R.id.med_section);
                break;
            case "TODO":
                navController.navigate(R.id.todoListFragment);
                break;
            case "FOOD":
                navController.navigate(R.id.nav_inventory);
                break;
            default:
                navController.navigate(R.id.nav_home);
                break;
        }
    }
}
