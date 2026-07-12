package com.example.fridgewise;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            Fragment selectedFragment = null;
            int itemId = menuItem.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new homeFragment();
            } else if (itemId == R.id.nav_inventory) {
                selectedFragment = new inventoryuFragment();
            } else if (itemId == R.id.nav_ai) {
                selectedFragment = new chefassistentFragment();
            } else if (itemId == R.id.nav_alerts) {
                selectedFragment = new alertsFragment();
            } else if (itemId == R.id.nav_pfp) {
                selectedFragment = new profileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainerView2, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Set default fragment on first launch
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView2, new homeFragment())
                    .commit();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
