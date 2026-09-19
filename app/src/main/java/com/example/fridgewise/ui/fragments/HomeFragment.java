package com.example.fridgewise.ui.fragments;

import com.example.fridgewise.R;
import com.example.fridgewise.data.*;
import com.example.fridgewise.model.*;
import com.example.fridgewise.adapter.*;
import com.example.fridgewise.util.*;
import com.example.fridgewise.ui.viewmodel.*;
import com.example.fridgewise.ui.activities.*;
import com.example.fridgewise.ui.bottomsheet.*;

import com.example.fridgewise.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.transition.TransitionManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.Navigation;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executors;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.ImageButton;
import com.google.android.material.chip.ChipGroup;

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
    }

    private void setupDashboard(View view) {
        /*
        View ivSparkle = view.findViewById(R.id.iv_sparkle_1);
        if (ivSparkle != null) {
            ivSparkle.animate().rotation(360f).scaleX(1.2f).scaleY(1.2f).setDuration(3000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ivSparkle.animate().rotation(0f).scaleX(1.0f).scaleY(1.0f).setDuration(3000).start();
                }
            }).start();
        }
        */

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
                tvUserName.setText("Hello, " + state.userName);
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
        TextInputEditText etShortCode = view.findViewById(R.id.et_home_short_code);
        ImageButton btnSubmit = view.findViewById(R.id.btn_quick_add_submit);
        ChipGroup cgFeedback = view.findViewById(R.id.cg_home_parsing_feedback);

        if (etShortCode == null || btnSubmit == null || cgFeedback == null) return;

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
                    } else {
                        etShortCode.setHint("What did you bring home? Try '3 Milk exp 12/28'");
                        cgFeedback.setVisibility(View.GONE);
                    }
                } else {
                    etShortCode.setHint("What did you bring home? Try '3 Milk exp 12/28'");
                    cgFeedback.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSubmit.setOnClickListener(v -> {
            String input = etShortCode.getText().toString();
            UniversalInputParser.ParsedResult result = UniversalInputParser.parse(input);
            
            if (result != null && result.isValid) {
                showParsingResult(result);
                etShortCode.setText("");
                cgFeedback.setVisibility(View.GONE);
            } else {
                Toast.makeText(getContext(), "Invalid format. Use section_value_...", Toast.LENGTH_SHORT).show();
            }
        });
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
        if (llHeader != null && llOptions != null) {
            llHeader.setOnClickListener(v -> {
                int vis = llOptions.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
                TransitionManager.beginDelayedTransition((ViewGroup) view);
                llOptions.setVisibility(vis);
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
