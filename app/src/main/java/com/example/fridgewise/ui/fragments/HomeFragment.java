package com.example.fridgewise.ui.fragments;

import com.example.fridgewise.R;
import com.example.fridgewise.data.*;
import com.example.fridgewise.model.*;
import com.example.fridgewise.adapter.*;
import com.example.fridgewise.util.*;
import com.example.fridgewise.ui.viewmodel.*;
import com.example.fridgewise.ui.activities.*;
import com.example.fridgewise.ui.bottomsheet.*;
import com.example.fridgewise.util.PermissionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.fridgewise.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.Settings;
import android.transition.AutoTransition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.transition.TransitionManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executors;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.ImageButton;
import com.google.android.material.chip.ChipGroup;

import android.content.res.ColorStateList;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private RecipeAdapter recipeAdapter;
    private AttentionAdapter attentionAdapter;
    private RecentActivityAdapter recentAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        recipeAdapter = new RecipeAdapter(recipe -> {
            RecipeDetailsBottomSheet sheet = RecipeDetailsBottomSheet.newInstance(recipe);
            sheet.show(getChildFragmentManager(), "recipe_details");
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setupDashboard(view);
        setupNavigation(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshDashboard();
        }
        checkReminderHealth(getView());
    }

    private void checkReminderHealth(View view) {
        if (view == null) return;
        MaterialCardView cvHealth = view.findViewById(R.id.cv_health_check);
        View btnFix = view.findViewById(R.id.btn_fix_reminders);
        ImageView ivIcon = view.findViewById(R.id.iv_health_check_icon);
        TextView tvTitle = view.findViewById(R.id.tv_health_check_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_health_check_subtitle);
        
        if (cvHealth == null || btnFix == null) return;
        
        boolean hasNotification = PermissionManager.hasNotificationPermission(requireContext());
        boolean canScheduleAlarms = PermissionManager.canScheduleExactAlarms(requireContext());
        
        if (hasNotification && canScheduleAlarms) {
            cvHealth.setVisibility(View.GONE);
        } else {
            cvHealth.setVisibility(View.VISIBLE);
            if (!hasNotification) {
                cvHealth.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.card_red)));
                cvHealth.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.badge_red_text)));
                if (ivIcon != null) {
                    ivIcon.setImageResource(R.drawable.ic_attention_red);
                    ivIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.badge_red_text)));
                }
                if (tvTitle != null) {
                    tvTitle.setText("Notifications are Disabled");
                    tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.badge_red_text));
                }
                if (tvSubtitle != null) {
                    tvSubtitle.setText("Enable permissions to get alerts and reminders.");
                }
                
                btnFix.setOnClickListener(v -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                });
            } else {
                cvHealth.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.card_orange)));
                cvHealth.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.badge_orange_text)));
                if (ivIcon != null) {
                    ivIcon.setImageResource(R.drawable.ic_attention_red);
                    ivIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.badge_orange_text)));
                }
                if (tvTitle != null) {
                    tvTitle.setText("Alarms & Reminders are Blocked");
                    tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.badge_orange_text));
                }
                if (tvSubtitle != null) {
                    tvSubtitle.setText("Allow exact alarms in Special App Access for precise alerts.");
                }
                
                btnFix.setOnClickListener(v -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });
            }
        }
    }

    private void setupDashboard(View view) {
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refreshDashboard());
            swipeRefreshLayout.setColorSchemeResources(R.color.green_primary);
        }

        LinearLayout llAttentionSection = view.findViewById(R.id.ll_attention_section);
        RecyclerView rvAttention = view.findViewById(R.id.rv_attention);
        TextView tvAttentionCount = view.findViewById(R.id.tv_attention_count);
        TextView tvUserMessage = view.findViewById(R.id.tv_user_message);
        
        RecyclerView rvRecent = view.findViewById(R.id.rv_recent_activity);
        recentAdapter = new RecentActivityAdapter(getContext());
        if (rvRecent != null) {
            rvRecent.setLayoutManager(new LinearLayoutManager(getContext()));
            rvRecent.setAdapter(recentAdapter);
        }
        
        // Setup Attention RecyclerView
        attentionAdapter = new AttentionAdapter(getContext(), item -> {
            // Handle click
            handleAttentionClick(item);
        });
        if (rvAttention != null) {
            rvAttention.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            rvAttention.setAdapter(attentionAdapter);
        }

        // Setup Recipe RecyclerView
        /*
        RecyclerView rvRecipes = view.findViewById(R.id.rv_suggested_recipes);
        if (rvRecipes != null) {
            rvRecipes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            rvRecipes.setAdapter(recipeAdapter);
        }
        View llRecipesSection = view.findViewById(R.id.ll_recipes_section);
        */
        View llRecipesSection = null;
        View cvSmartTip = view.findViewById(R.id.cv_smart_tip);
        TextView tvSmartTip = view.findViewById(R.id.tv_smart_tip_text);

        View cvTodayInsight = view.findViewById(R.id.cv_today_insight);
        TextView tvInsightTitle = view.findViewById(R.id.tv_insight_title);
        TextView tvInsightDesc = view.findViewById(R.id.tv_insight_description);
        View btnInsightAction = view.findViewById(R.id.btn_insight_action);
        View btnViewAllAttention = view.findViewById(R.id.tv_view_all_attention);

        if (btnViewAllAttention != null) {
            btnViewAllAttention.setOnClickListener(v -> {
                NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager()
                        .findFragmentById(R.id.fragmentContainerView2);
                if (navHostFragment != null) {
                    navHostFragment.getNavController().navigate(R.id.nav_alerts);
                }
            });
        }

        Context context = getContext();
        if (context == null) return;

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(state.isLoading);
            }

            // Update Greeting & Name
            if (tvUserMessage != null && state.greeting != null) {
                if (!state.greeting.equals(tvUserMessage.getText().toString())) {
                    tvUserMessage.setAlpha(0f);
                    tvUserMessage.setText(state.greeting);
                    tvUserMessage.animate().alpha(1f).setDuration(500).start();
                }
            }
            
            TextView tvUserName = view.findViewById(R.id.tv_user_name);
            if (tvUserName != null && state.userName != null) {
                PreferenceManager pm = new PreferenceManager(requireContext());
                if (pm.getUserId() != null) {
                    tvUserName.setText("Hello, " + pm.getUserId());
                } else {
                    tvUserName.setText("Hello, " + state.userName);
                }
            }

            // Update Recent Activities
            if (state.recentActivities != null && recentAdapter != null) {
                recentAdapter.setActivities(state.recentActivities);
                if (view.findViewById(R.id.textView8) != null) {
                    view.findViewById(R.id.textView8).setVisibility(state.recentActivities.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }
            
            // Update Recipes
            if (state.suggestedRecipes != null && !state.suggestedRecipes.isEmpty() && recipeAdapter != null) {
                if (llRecipesSection != null) llRecipesSection.setVisibility(View.VISIBLE);
                if (cvSmartTip != null) cvSmartTip.setVisibility(View.GONE);
                recipeAdapter.setRecipes(state.suggestedRecipes);
            } else {
                if (llRecipesSection != null) llRecipesSection.setVisibility(View.GONE);
                if (cvSmartTip != null) {
                    cvSmartTip.setVisibility(View.VISIBLE);
                    if (tvSmartTip != null) tvSmartTip.setText(state.smartTip);
                }
            }

            // Update Attention Section
            if (state.attentionItems != null && !state.attentionItems.isEmpty()) {
                if (llAttentionSection != null) llAttentionSection.setVisibility(View.VISIBLE);
                if (rvAttention != null) rvAttention.setVisibility(View.VISIBLE);
                attentionAdapter.submitList(new ArrayList<>(state.attentionItems));
                
                View emptyView = view.findViewById(R.id.cv_attention_empty);
                if (emptyView != null) emptyView.setVisibility(View.GONE);
                
                if (tvAttentionCount != null) tvAttentionCount.setText(state.attentionItems.size() + " things need attention");
            } else {
                View emptyView = view.findViewById(R.id.cv_attention_empty);
                if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                if (rvAttention != null) rvAttention.setVisibility(View.GONE);
                if (tvAttentionCount != null) tvAttentionCount.setText("Everything is in order");
            }

            // Update Insight Card
            if (cvTodayInsight != null) {
                cvTodayInsight.setVisibility(View.VISIBLE);
                if (state.isLoading) {
                    tvInsightTitle.setText("Thinking...");
                    tvInsightDesc.setText("Analyzing your fridge data...");
                    if (btnInsightAction != null) btnInsightAction.setVisibility(View.GONE);
                } else {
                    tvInsightTitle.setText(state.insightTitle != null ? state.insightTitle : "Fridge Insight");
                    tvInsightDesc.setText(state.insightDescription != null ? state.insightDescription : "Analyzing...");
                    
                    if (btnInsightAction != null) {
                        btnInsightAction.setVisibility(state.hasActionableItems ? View.VISIBLE : View.GONE);
                        btnInsightAction.setOnClickListener(v -> {
                             viewModel.autoAddActionableToShoppingList();
                        });
                    }
                }
            }
        });

        setupUniversalInput(view);

        viewModel.getActionMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupUniversalInput(View view) {
        View btnToggle = view.findViewById(R.id.btn_toggle_shortcode);
        View llContainer = view.findViewById(R.id.ll_home_shortcode_container);
        TextInputEditText etShortCode = view.findViewById(R.id.et_home_short_code);
        ImageButton btnSubmit = view.findViewById(R.id.btn_quick_add_submit);
        ChipGroup cgFeedback = view.findViewById(R.id.cg_home_parsing_feedback);
        ChipGroup cgSuggestions = view.findViewById(R.id.cg_home_input_suggestions);
        View cvResult = view.findViewById(R.id.cv_universal_result);
        ImageView ivResultIcon = view.findViewById(R.id.iv_result_icon);
        TextView tvResultTitle = view.findViewById(R.id.tv_result_title);
        TextView tvResultDetails = view.findViewById(R.id.tv_result_details);
        ImageButton btnCloseResult = view.findViewById(R.id.btn_close_result);

        if (btnToggle != null && llContainer != null) {
            btnToggle.setOnClickListener(v -> {
                boolean isVisible = llContainer.getVisibility() == View.VISIBLE;
                AutoTransition transition = new AutoTransition();
                transition.setDuration(200);
                TransitionManager.beginDelayedTransition((ViewGroup) view.getRootView(), transition);
                if (isVisible) {
                    llContainer.setVisibility(View.GONE);
                    btnToggle.animate().rotation(0f).setDuration(200).start();
                } else {
                    llContainer.setVisibility(View.VISIBLE);
                    btnToggle.animate().rotation(45f).setDuration(200).start();
                }
            });
        }

        if (etShortCode == null || btnSubmit == null || cgFeedback == null || cgSuggestions == null) return;

        // Setup Suggestion Chips
        setupSuggestionChips(cgSuggestions, etShortCode);

        etShortCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (input.contains("_")) {
                    String prefix = input.split("_")[0];
                    if (UniversalInputParser.isSection(prefix)) {
                        etShortCode.setHint(UniversalInputParser.getHint(prefix));
                        updateParsingFeedback(cgFeedback, input);
                        updateAssistChips(cgSuggestions, etShortCode, prefix);
                        if (cvResult != null) cvResult.setVisibility(View.GONE); 
                    } else {
                        resetInputUI(etShortCode, cgFeedback, cgSuggestions);
                    }
                } else {
                    resetInputUI(etShortCode, cgFeedback, cgSuggestions);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSubmit.setOnClickListener(v -> {
            String input = etShortCode.getText().toString();
            UniversalInputParser.ParsedResult result = UniversalInputParser.parse(input);
            
            if (result != null && result.isValid) {
                // Call ViewModel for real persistence
                viewModel.processUniversalInput(input);
                
                // Visual feedback
                showPolishedResult(result, cvResult, ivResultIcon, tvResultTitle, tvResultDetails);
                etShortCode.setText("");
                cgFeedback.setVisibility(View.GONE);
            } else {
                Toast.makeText(getContext(), "Invalid format. Use name_value_...", Toast.LENGTH_SHORT).show();
            }
        });

        if (btnCloseResult != null) {
            btnCloseResult.setOnClickListener(v -> cvResult.setVisibility(View.GONE));
        }
    }

    private void setupSuggestionChips(ChipGroup cgSuggestions, TextInputEditText etShortCode) {
        cgSuggestions.removeAllViews();
        for (String section : UniversalInputParser.getSections()) {
            UniversalInputParser.SectionMetadata meta = UniversalInputParser.getMetadata(section);
            if (meta == null) continue;

            Chip chip = new Chip(getContext());
            chip.setText("+ " + meta.title.split(" ")[0]); // e.g., "+ Task"
            chip.setChipIcon(ContextCompat.getDrawable(requireContext(), meta.iconRes));
            chip.setChipIconSize(dpToPx(18));
            chip.setIconStartPadding(dpToPx(4));
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.bg_secondary)));
            chip.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), meta.colorRes)));
            chip.setChipStrokeWidth((float) dpToPx(1));
            chip.setTextColor(ContextCompat.getColor(requireContext(), meta.colorRes));
            chip.setTextSize(12f);
            
            chip.setOnClickListener(v -> {
                etShortCode.setText(section + "_");
                etShortCode.setSelection(etShortCode.getText().length());
                etShortCode.requestFocus();
            });
            cgSuggestions.addView(chip);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void updateAssistChips(ChipGroup cgSuggestions, TextInputEditText etShortCode, String prefix) {
        UniversalInputParser.SectionMetadata meta = UniversalInputParser.getMetadata(prefix);
        if (meta == null) return;

        cgSuggestions.removeAllViews();
        for (String field : meta.allFields) {
            Chip chip = new Chip(getContext());
            chip.setText(field.substring(0, 1).toUpperCase() + field.substring(1));
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.bg_primary)));
            chip.setTextColor(ContextCompat.getColor(requireContext(), meta.colorRes));
            chip.setChipStrokeWidth((float) dpToPx(1));
            chip.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), meta.colorRes)));
            chip.setTextSize(11f);

            chip.setOnClickListener(v -> {
                String current = etShortCode.getText().toString();
                if (!current.endsWith("_")) {
                    etShortCode.setText(current + "_");
                }
                etShortCode.setSelection(etShortCode.getText().length());
                etShortCode.requestFocus();
            });
            cgSuggestions.addView(chip);
        }
    }

    private void resetInputUI(TextInputEditText etShortCode, ChipGroup cgFeedback, ChipGroup cgSuggestions) {
        etShortCode.setHint("What did you bring home? Try '3 Milk exp 12/28'");
        cgFeedback.setVisibility(View.GONE);
        setupSuggestionChips(cgSuggestions, etShortCode);
    }

    private void showPolishedResult(UniversalInputParser.ParsedResult result, View cvResult, ImageView ivIcon, TextView tvTitle, TextView tvDetails) {
        UniversalInputParser.SectionMetadata meta = UniversalInputParser.getMetadata(result.section);
        if (meta == null || cvResult == null) return;

        ivIcon.setImageResource(meta.iconRes);
        ivIcon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), meta.colorRes)).withAlpha(30));
        ivIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), meta.colorRes)));

        tvTitle.setText("Added to " + meta.title);
        tvTitle.setTextColor(ContextCompat.getColor(requireContext(), meta.colorRes));

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Map.Entry<String, String> entry : result.values.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue());
            if (++count < result.values.size()) sb.append("  •  ");
        }
        tvDetails.setText(sb.toString());

        // Animate result card entry
        cvResult.setVisibility(View.VISIBLE);
        cvResult.setAlpha(0f);
        cvResult.setTranslationY(20f);
        cvResult.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .start();
    }

    private void updateParsingFeedback(ChipGroup cgFeedback, String input) {
        UniversalInputParser.ParsedResult result = UniversalInputParser.parse(input);
        cgFeedback.removeAllViews();
        
        if (result != null && !result.values.isEmpty()) {
            cgFeedback.setVisibility(View.VISIBLE);
            for (String field : result.fields) {
                String value = result.values.get(field);
                if (value != null) {
                    Chip chip = new Chip(getContext());
                    chip.setText(field + ": " + value);
                    chip.setChipBackgroundColorResource(R.color.bg_primary);
                    chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_primary));
                    cgFeedback.addView(chip);
                }
            }
        } else {
            cgFeedback.setVisibility(View.GONE);
        }
    }

    private void showParsingResult(UniversalInputParser.ParsedResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Added to ").append(result.section.toUpperCase()).append(":\n");
        for (Map.Entry<String, String> entry : result.values.entrySet()) {
            sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        // Simple Toast for now as requested
        Toast.makeText(getContext(), sb.toString(), Toast.LENGTH_LONG).show();
        
        // Log it too
        Log.d("UniversalInput", sb.toString());
    }

    private void handleAttentionClick(AttentionItem item) {
        if (item == null) return;
        
        Context context = getContext();
        if (context == null) return;
        
        AppDatabase db = AppDatabase.getInstance(context);
        Executors.newSingleThreadExecutor().execute(() -> {
            Bundle args = new Bundle();
            int navAction = -1;
            
            try {
                int id = Integer.parseInt(item.getId());
                switch (item.getType()) {
                    case FOOD:
                        FoodItem food = db.foodItemDao().getItemById(id);
                        if (food != null) {
                            args.putSerializable("foodItem", food);
                            navAction = R.id.addItemFragment;
                        }
                        break;
                    case MEDICINE:
                        MedicineEntity med = db.medicineDao().getMedicineById(id);
                        if (med != null) {
                            args.putSerializable(MedicineAddFragment.ARG_MEDICINE, med);
                            navAction = R.id.medicineAddFragment;
                        }
                        break;
                    case TODO:
                        TodoItem todo = db.todoDao().getTodoById(id);
                        if (todo != null) {
                            args.putSerializable(AddTodoFragment.ARG_TODO_ITEM, todo);
                            navAction = R.id.addTodoFragment;
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (navAction != -1 && isAdded()) {
                final int finalNavAction = navAction;
                requireActivity().runOnUiThread(() -> {
                    try {
                        Navigation.findNavController(requireView()).navigate(finalNavAction, args);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void setupNavigation(View view) {
        View ivProfile = view.findViewById(R.id.pfp01);
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
                try {
                    BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_pfp);
                    } else {
                        Navigation.findNavController(v).navigate(R.id.nav_pfp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
            // Set avatar if customized locally
            PreferenceManager pm = new PreferenceManager(requireContext());
            if (pm.getProfileImageUri() != null && !pm.getProfileImageUri().isEmpty()) {
                try {
                    ((ImageView) ivProfile).setImageURI(Uri.parse(pm.getProfileImageUri()));
                } catch (Exception e) {
                    ((ImageView) ivProfile).setImageResource(R.drawable.ic_default_avatar);
                }
            } else {
                ((ImageView) ivProfile).setImageResource(R.drawable.ic_default_avatar);
            }
        }

        View btnAddFood = view.findViewById(R.id.btn_add_food);
        if (btnAddFood != null) btnAddFood.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.addItemFragment));
        
        View btnAddMed = view.findViewById(R.id.btn_add_medicine);
        if (btnAddMed != null) btnAddMed.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.medicineAddFragment));
        
        View btnAddTodo = view.findViewById(R.id.btn_add_todo);
        if (btnAddTodo != null) btnAddTodo.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.addTodoFragment));

        View btnAddShopping = view.findViewById(R.id.btn_add_shopping);
        if (btnAddShopping != null) btnAddShopping.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.shoppingListFragment));

        View btnAddDoc = view.findViewById(R.id.btn_add_document);
        if (btnAddDoc != null) btnAddDoc.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.addDocumentFragment));
        
        LinearLayout llHeader = view.findViewById(R.id.ll_quick_add_header);
        LinearLayout llOptions = view.findViewById(R.id.ll_quick_add_options);
        View ivArrow = view.findViewById(R.id.iv_expand_arrow);
        if (llHeader != null && llOptions != null) {
            llHeader.setOnClickListener(v -> {
                boolean isVisible = llOptions.getVisibility() == View.VISIBLE;
                AutoTransition transition = new AutoTransition();
                transition.setDuration(200);
                TransitionManager.beginDelayedTransition((ViewGroup) view.getRootView(), transition);
                if (isVisible) {
                    llOptions.setVisibility(View.GONE);
                    if (ivArrow != null) ivArrow.animate().rotation(0f).setDuration(200).start();
                } else {
                    llOptions.setVisibility(View.VISIBLE);
                    if (ivArrow != null) ivArrow.animate().rotation(90f).setDuration(200).start();
                }
            });
        }

        SearchView searchBar = view.findViewById(R.id.home_searchbar);
        View cvSearch = view.findViewById(R.id.cv_home_search);

        if (cvSearch != null && searchBar != null) {
            cvSearch.setOnClickListener(v -> {
                searchBar.setIconified(false);
                searchBar.requestFocus();
            });

            searchBar.setOnClickListener(v -> {
                searchBar.setIconified(false);
                searchBar.requestFocus();
            });
        }

        if (searchBar != null) {
            searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (query != null && !query.isEmpty()) {
                        Bundle args = new Bundle();
                        args.putString("search_query", query);
                        Navigation.findNavController(view).navigate(R.id.globalSearchFragment, args);
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
    }
}
