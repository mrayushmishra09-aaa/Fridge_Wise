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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InventoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private FoodAdapter adapter;
    private View llEmptyState, selectionToolbar, headerLayout;
    private TextView tvSelectionCount;
    private CheckBox cbSelectAll;
    private List<FoodItem> allFoodItems = new ArrayList<>();
    private List<FoodItem> currentFilteredItems = new ArrayList<>();
    private String currentCategory = "All";
    private String currentSearchQuery = "";
    private final Executor executor = Executors.newSingleThreadExecutor();

    public InventoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventoryu, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        
        if (getArguments() != null) {
            currentSearchQuery = getArguments().getString("search_query", "");
        }

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_inventory);
        llEmptyState = view.findViewById(R.id.ll_inventory_empty_state);
        selectionToolbar = view.findViewById(R.id.selectionToolbar);
        headerLayout = view.findViewById(R.id.layoutStandardHeader);
        tvSelectionCount = view.findViewById(R.id.tvSelectionCount);
        cbSelectAll = view.findViewById(R.id.cbSelectAll);

        view.findViewById(R.id.btnCloseSelection).setOnClickListener(v -> exitSelectionMode());
        view.findViewById(R.id.btnBulkDelete).setOnClickListener(v -> bulkDelete());
        View btnBulkShare = view.findViewById(R.id.btnBulkShare);
        if (btnBulkShare != null) {
            btnBulkShare.setOnClickListener(v -> bulkShare());
        }

        if (cbSelectAll != null) {
            cbSelectAll.setOnClickListener(v -> {
                if (cbSelectAll.isChecked()) adapter.selectAll();
                else adapter.clearSelection();
            });
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FoodAdapter(new FoodAdapter.onItemClickListener() {
            @Override
            public void onEditClick(FoodItem foodItem) {
                Bundle args = new Bundle();
                args.putSerializable("foodItem", (Serializable) foodItem);
                Navigation.findNavController(view).navigate(R.id.addItemFragment, args);
            }

            @Override
            public void onDeleteClick(FoodItem foodItem) {
                deleteItem(foodItem);
            }

            @Override
            public void onInfoClick(FoodItem foodItem) {
                FoodInfoBottomSheet sheet = FoodInfoBottomSheet.newInstance(foodItem);
                sheet.show(getChildFragmentManager(), "food_info");
            }

            @Override
            public void onSelectionModeChanged(boolean isSelectionMode) {
                if (isSelectionMode) enterSelectionMode();
                else exitSelectionMode();
            }

            @Override
            public void onSelectionCountChanged(int count) {
                if (tvSelectionCount != null) {
                    if (count > 0 && count == currentFilteredItems.size()) {
                        tvSelectionCount.setText("All selected");
                        if (cbSelectAll != null) cbSelectAll.setChecked(true);
                    } else {
                        tvSelectionCount.setText(count + " selected");
                        if (cbSelectAll != null) cbSelectAll.setChecked(false);
                    }
                }
            }
        });
        recyclerView.setAdapter(adapter);

        setupSwipeToDelete();

        // Setup Category Chips
        ChipGroup chipGroup = view.findViewById(R.id.category_chip_group);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.all_chip) currentCategory = "All";
            else if (checkedId == R.id.dairy_chip) currentCategory = "Dairy";
            else if (checkedId == R.id.veg_chip) currentCategory = "Vegetable";
            else if (checkedId == R.id.fruit_chip) currentCategory = "Fruits";
            else if (checkedId == R.id.nonveg_chip) currentCategory = "Non-veg";
            else if (checkedId == R.id.drinks_chip) currentCategory = "Drinks";
            else if (checkedId == R.id.frozen_chip) currentCategory = "Frozen-Food";
            else if (checkedId == R.id.snacks_chip) currentCategory = "Snacks";
            else if (checkedId == R.id.bakery_chip) currentCategory = "Bakery";
            else if (checkedId == R.id.others_chip) currentCategory = "others";
            
            filterItems();
        });

        // Setup Search
        SearchView searchView = view.findViewById(R.id.inventory_search_view);
        View cvSearch = view.findViewById(R.id.cv_inventory_search);

        if (cvSearch != null && searchView != null) {
            cvSearch.setOnClickListener(v -> {
                searchView.setIconified(false);
                searchView.requestFocus();
            });

            searchView.setOnClickListener(v -> {
                searchView.setIconified(false);
                searchView.requestFocus();
            });
        }

        if (searchView != null) {
            if (!currentSearchQuery.isEmpty()) {
                searchView.setQuery(currentSearchQuery, false);
                searchView.setIconified(false);
            }
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    currentSearchQuery = query;
                    filterItems();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    currentSearchQuery = newText;
                    filterItems();
                    return true;
                }
            });
        }

        // Fetch items from Database
        loadItems();

        FloatingActionButton fab = view.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.addItemFragment);
        });
    }

    private void loadItems() {
        Context context = getContext();
        if (context == null) return;
        AppDatabase db = AppDatabase.getInstance(context);
        executor.execute(() -> {
            List<FoodItem> items = db.foodItemDao().getAllItems();
            allFoodItems = items;
            
            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(this::filterItems);
            }
        });
    }

    private void filterItems() {
        executor.execute(() -> {
            List<FoodItem> filteredList = new ArrayList<>();
            
            for (FoodItem item : allFoodItems) {
                boolean matchesCategory = currentCategory.equals("All") || 
                                         item.getCategory().equalsIgnoreCase(currentCategory);
                
                boolean matchesSearch = currentSearchQuery.isEmpty() || 
                                       item.getName().toLowerCase().contains(currentSearchQuery.trim().toLowerCase());
                
                if (matchesCategory && matchesSearch) {
                    filteredList.add(item);
                }
            }
            
            Activity activity = getActivity();
            if (activity != null && isAdded()) {
                activity.runOnUiThread(() -> {
                    currentFilteredItems = filteredList;
                    if (adapter != null) {
                        adapter.setFoodList(filteredList);
                    }

                    // Toggle empty state visibility
                    if (llEmptyState != null) {
                        if (filteredList.isEmpty()) {
                            llEmptyState.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            llEmptyState.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }

    private void deleteItem(FoodItem foodItem) {
        Context context = getContext();
        if (context == null) return;
        AppDatabase db = AppDatabase.getInstance(context);
        executor.execute(() -> {
            db.foodItemDao().delete(foodItem);
            loadItems();
        });
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                FoodItem itemToDelete = adapter.getFoodList().get(position);
                deleteItem(itemToDelete);
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void enterSelectionMode() {
        if (selectionToolbar != null) selectionToolbar.setVisibility(View.VISIBLE);
        if (headerLayout != null) headerLayout.setVisibility(View.GONE);
    }

    private void exitSelectionMode() {
        if (selectionToolbar != null) selectionToolbar.setVisibility(View.GONE);
        if (headerLayout != null) headerLayout.setVisibility(View.VISIBLE);
        if (adapter != null) adapter.clearSelection();
        if (cbSelectAll != null) cbSelectAll.setChecked(false);
    }

    private void bulkDelete() {
        List<FoodItem> selectedItems = adapter.getSelectedItems();
        if (selectedItems.isEmpty()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete " + selectedItems.size() + " items?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete Forever", (dialog, which) -> {
                    executor.execute(() -> {
                        AppDatabase db = AppDatabase.getInstance(requireContext());
                        for (FoodItem item : selectedItems) {
                            db.foodItemDao().delete(item);
                        }
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Items deleted", Toast.LENGTH_SHORT).show();
                                exitSelectionMode();
                                loadItems();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void bulkShare() {
        List<FoodItem> selectedItems = adapter.getSelectedItems();
        if (selectedItems.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("🍎 Shared Inventory from FridgeWise:\n\n");
        for (int i = 0; i < selectedItems.size(); i++) {
            FoodItem item = selectedItems.get(i);
            sb.append(i + 1).append(". ").append(item.getName());
            sb.append(" (").append(item.getQuantity()).append(" ").append(item.getUnit()).append(")");
            if (item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()) {
                sb.append(" - Expiry: ").append(item.getExpiryDate());
            }
            sb.append("\n");
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share inventory via");
        startActivity(shareIntent);
    }
}
