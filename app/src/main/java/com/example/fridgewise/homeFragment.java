package com.example.fridgewise;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.transition.TransitionManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;

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
        LinearLayout llAttentionSection = view.findViewById(R.id.ll_attention_section);
        RecyclerView rvAttention = view.findViewById(R.id.rv_attention);
        TextView tvAttentionCount = view.findViewById(R.id.tv_attention_count);
        TextView tvUserMessage = view.findViewById(R.id.tv_user_message);
        
        RecyclerView rvRecent = view.findViewById(R.id.rv_recent_activity);
        RecentActivityAdapter recentAdapter = new RecentActivityAdapter(getContext());
        if (rvRecent != null) {
            rvRecent.setLayoutManager(new LinearLayoutManager(getContext()));
            rvRecent.setAdapter(recentAdapter);
        }

        View cvTodayInsight = view.findViewById(R.id.cv_today_insight);
        TextView tvInsightTitle = view.findViewById(R.id.tv_insight_title);
        TextView tvInsightDesc = view.findViewById(R.id.tv_insight_description);
        View btnInsightAction = view.findViewById(R.id.btn_insight_action);
        View btnViewAllAttention = view.findViewById(R.id.tv_view_all_attention);

        if (btnViewAllAttention != null) {
            btnViewAllAttention.setOnClickListener(v -> replaceFragment(new AlertsFragment()));
        }

        Context context = getContext();
        if (context == null) return;

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            // Update Greeting & Name
            if (tvUserMessage != null) tvUserMessage.setText(state.greeting);
            
            TextView tvUserName = view.findViewById(R.id.tv_user_name);
            if (tvUserName != null && state.userName != null) {
                tvUserName.setText("Hello, " + state.userName);
            }

            // Update Recent Activities
            if (state.recentActivities != null) {
                recentAdapter.setActivities(state.recentActivities);
                if (view.findViewById(R.id.textView8) != null) {
                    view.findViewById(R.id.textView8).setVisibility(state.recentActivities.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            // Update Attention Section
            if (state.attentionItems != null && !state.attentionItems.isEmpty()) {
                llAttentionSection.setVisibility(View.VISIBLE);
                rvAttention.setVisibility(View.VISIBLE);
                View emptyView = view.findViewById(R.id.cv_attention_empty);
                if (emptyView != null) emptyView.setVisibility(View.GONE);
                
                tvAttentionCount.setText(state.attentionItems.size() + " things need attention");
                rvAttention.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                rvAttention.setAdapter(new AttentionAdapter(context, state.attentionItems, item -> {
                     // TODO: Add Navigation logic here based on item.getType()
                }));
            } else {
                View emptyView = view.findViewById(R.id.cv_attention_empty);
                if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                rvAttention.setVisibility(View.GONE);
                tvAttentionCount.setText("Everything is in order");
            }

            // Update Insight Card
            if (cvTodayInsight != null) {
                cvTodayInsight.setVisibility(View.VISIBLE);
                if (state.isLoading) {
                    tvInsightTitle.setText("Thinking...");
                    tvInsightDesc.setText("Analyzing your fridge data...");
                } else {
                    tvInsightTitle.setText(state.insightTitle);
                    tvInsightDesc.setText(state.insightDescription);
                    
                    if (btnInsightAction != null) {
                        btnInsightAction.setOnClickListener(v -> {
                             // Navigate to Shopping list as a default helpful action
                             replaceFragment(new ShoppingListFragment());
                        });
                    }
                }
            }
        });
    }

    private void setupNavigation(View view) {
        View btnAddFood = view.findViewById(R.id.btn_add_food);
        if (btnAddFood != null) btnAddFood.setOnClickListener(v -> replaceFragment(new AddItemFragment()));
        
        View btnAddMed = view.findViewById(R.id.btn_add_medicine);
        if (btnAddMed != null) btnAddMed.setOnClickListener(v -> replaceFragment(new MedicineAddFragment()));
        
        View btnAddTodo = view.findViewById(R.id.btn_add_todo);
        if (btnAddTodo != null) btnAddTodo.setOnClickListener(v -> replaceFragment(new AddTodoFragment()));
        
        LinearLayout llHeader = view.findViewById(R.id.ll_quick_add_header);
        LinearLayout llOptions = view.findViewById(R.id.ll_quick_add_options);
        if (llHeader != null && llOptions != null) {
            llHeader.setOnClickListener(v -> {
                int vis = llOptions.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
                TransitionManager.beginDelayedTransition((ViewGroup) view);
                llOptions.setVisibility(vis);
            });
        }

        android.widget.SearchView searchBar = view.findViewById(R.id.home_searchbar);
        if (searchBar != null) {
            searchBar.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (query != null && !query.isEmpty()) {
                        InventoryFragment fragment = new InventoryFragment();
                        Bundle args = new Bundle();
                        args.putString("search_query", query);
                        fragment.setArguments(args);
                        replaceFragment(fragment);
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

    private void replaceFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView2, fragment)
                .addToBackStack(null)
                .commit();
    }
}
