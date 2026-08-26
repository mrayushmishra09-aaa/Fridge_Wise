package com.example.fridgewise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomSpaceInventoryFragment extends Fragment {

    private static final String ARG_SPACE = "arg_space";
    private CustomSpace currentSpace;
    private RecyclerView recyclerView;
    private CustomSpaceItemAdapter adapter;
    private List<CustomSpaceItem> allItems = new ArrayList<>();
    
    private TextView tvBannerMsg, tvProgressPercent;
    private com.google.android.material.progressindicator.LinearProgressIndicator progressOverall;
    private View layoutBanner, layoutEmptyState;

    public static CustomSpaceInventoryFragment newInstance(CustomSpace space) {
        CustomSpaceInventoryFragment fragment = new CustomSpaceInventoryFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SPACE, space);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentSpace = (CustomSpace) getArguments().getSerializable(ARG_SPACE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_custom_space_inventory, container, false);

        TextView tvTitle = view.findViewById(R.id.tvSpaceTitle);
        tvTitle.setText(currentSpace.getName());

        tvBannerMsg = view.findViewById(R.id.tvBannerMsg);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        progressOverall = view.findViewById(R.id.progressOverall);
        layoutBanner = view.findViewById(R.id.layoutBanner);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        
        // Professional Polish: Set banner color based on space theme
        if (currentSpace.getColorCode() != 0) {
            layoutBanner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentSpace.getColorCode()));
            layoutBanner.setBackgroundTintMode(android.graphics.PorterDuff.Mode.SRC_ATOP);
        }
        
        // Hide progress elements if checkbox is not enabled for this space
        if (!currentSpace.isHasCheckbox()) {
            progressOverall.setVisibility(View.GONE);
            tvProgressPercent.setVisibility(View.GONE);
            view.findViewById(R.id.tvProgressLabel).setVisibility(View.GONE);
            tvBannerMsg.setText(String.format("Manage your %s effectively!", currentSpace.getName()));
        }

        recyclerView = view.findViewById(R.id.rvSpaceItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new CustomSpaceItemAdapter(new CustomSpaceItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CustomSpaceItem item) {
                // Edit item
                AddSpaceItemFragment fragment = AddSpaceItemFragment.newInstance(currentSpace.getId(), item);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView2, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDeleteClick(CustomSpaceItem item) {
                deleteItem(item);
            }

            @Override
            public void onCheckChanged(CustomSpaceItem item, boolean isChecked) {
                item.setChecked(isChecked);
                if (isChecked) {
                    item.setCompletionTimestamp(System.currentTimeMillis());
                } else {
                    item.setCompletionTimestamp(null);
                }
                Executors.newSingleThreadExecutor().execute(() -> {
                    AppDatabase.getInstance(requireContext()).customSpaceDao().updateItem(item);
                    // Refresh UI and Banner
                    requireActivity().runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        updateBannerProgress();
                    });
                });
            }
        });
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        view.findViewById(R.id.btnMoreOptions).setOnClickListener(this::showMoreOptions);

        view.findViewById(R.id.fabAddItem).setOnClickListener(v -> {
            AddSpaceItemFragment fragment = AddSpaceItemFragment.newInstance(currentSpace.getId(), null);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView2, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterItems(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterItems(newText);
                return true;
            }
        });

        loadItems();

        return view;
    }

    private void loadItems() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<CustomSpaceItem> items = db.customSpaceDao().getItemsForSpace(currentSpace.getId());
            
            // Handle auto-removal logic
            long now = System.currentTimeMillis();
            List<CustomSpaceItem> validItems = new ArrayList<>();
            int duration = currentSpace.getAutoRemoveDuration();
            
            if (duration > 0) {
                long durationMillis = duration * 24L * 60L * 60L * 1000L;
                for (CustomSpaceItem item : items) {
                    if (item.isChecked() && item.getCompletionTimestamp() != null) {
                        if (now - item.getCompletionTimestamp() > durationMillis) {
                            db.customSpaceDao().deleteItem(item);
                            continue;
                        }
                    }
                    validItems.add(item);
                }
                allItems = validItems;
            } else {
                allItems = items;
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    adapter.setItems(allItems, currentSpace);
                    updateBannerProgress();
                    
                    // Toggle Empty State
                    if (allItems.isEmpty()) {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        layoutEmptyState.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private void updateBannerProgress() {
        if (!currentSpace.isHasCheckbox() || allItems.isEmpty()) {
            return;
        }

        int total = allItems.size();
        int completed = 0;
        for (CustomSpaceItem item : allItems) {
            if (item.isChecked()) completed++;
        }

        int percent = (completed * 100) / total;
        progressOverall.setProgress(percent);
        tvProgressPercent.setText(String.format("%d%% Completed", percent));

        // Dynamic Professional Messages
        if (percent == 0) {
            tvBannerMsg.setText(String.format("Get started with your %s tasks!", currentSpace.getName()));
        } else if (percent < 50) {
            tvBannerMsg.setText(String.format("You're making progress on your %s!", currentSpace.getName()));
        } else if (percent < 100) {
            tvBannerMsg.setText(String.format("Almost there! Keep going with your %s.", currentSpace.getName()));
        } else {
            tvBannerMsg.setText(String.format("Excellent! All %s tasks completed.", currentSpace.getName()));
        }
    }

    private void showMoreOptions(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add("Edit Space");
        popup.getMenu().add("Delete Space");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle() != null ? item.getTitle().toString() : "";
            if ("Edit Space".equals(title)) {
                CreateSpaceFragment fragment = CreateSpaceFragment.newInstance(currentSpace);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView2, fragment)
                        .addToBackStack(null)
                        .commit();
            } else if ("Delete Space".equals(title)) {
                deleteSpace();
            }
            return true;
        });
        popup.show();
    }

    private void deleteSpace() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Space")
                .setMessage("Are you sure you want to delete this entire space? All items inside will be lost.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase db = AppDatabase.getInstance(requireContext());
                        // Delete space (CASCADE will handle items if DB configured, but we do it manually to be safe)
                        List<CustomSpaceItem> items = db.customSpaceDao().getItemsForSpace(currentSpace.getId());
                        for (CustomSpaceItem item : items) {
                            db.customSpaceDao().deleteItem(item);
                        }
                        db.customSpaceDao().deleteSpace(currentSpace);

                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Space deleted", Toast.LENGTH_SHORT).show();
                                getParentFragmentManager().popBackStack();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void filterItems(String query) {
        List<CustomSpaceItem> filtered = new ArrayList<>();
        for (CustomSpaceItem item : allItems) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(item);
            }
        }
        adapter.setItems(filtered, currentSpace);
    }

    private void deleteItem(CustomSpaceItem item) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase.getInstance(requireContext()).customSpaceDao().deleteItem(item);
                        loadItems();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
