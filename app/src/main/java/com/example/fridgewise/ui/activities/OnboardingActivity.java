package com.example.fridgewise.ui.activities;

import com.example.fridgewise.R;
import com.example.fridgewise.data.*;
import com.example.fridgewise.model.*;
import com.example.fridgewise.adapter.*;
import com.example.fridgewise.util.*;
import com.example.fridgewise.ui.viewmodel.*;
import com.example.fridgewise.ui.fragments.*;

import com.example.fridgewise.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnNext;
    private OnboardingAdapter adapter;
    private OnboardingViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 2) { // Last page (Registration / Login)
                    // Hide the bottom controls layout entirely on the Login page
                    findViewById(R.id.ll_bottom_controls).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.ll_bottom_controls).setVisibility(View.VISIBLE);
                    btnNext.setText("Next");
                }
            }
        });

        viewModel.getRegistrationSuccess().observe(this, success -> {
            if (success) {
                finishOnboarding();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < 2) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                viewModel.triggerSave();
            }
        });
    }

    public void finishOnboarding() {
        PreferenceManager prefManager = new PreferenceManager(this);
        prefManager.setFirstTimeLaunch(false);
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private static class OnboardingAdapter extends FragmentStateAdapter {

        public OnboardingAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return IntroFragment.newInstance("Welcome to FridgeWise", "Your smart space to remember and manage the things that matter every day.", R.drawable.start_img01_zoom);
                case 1: return IntroFragment.newInstance("Everything in one place", "Keep track of your items, medicines, tasks, shopping lists, and more.", "FridgeWise helps you remember what you have, what needs attention, and what comes next.", R.drawable.start_img02);
                case 2: return new RegistrationFragment();
                default: return new IntroFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
