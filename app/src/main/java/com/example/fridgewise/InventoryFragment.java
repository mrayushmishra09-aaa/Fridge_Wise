package com.example.fridgewise;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class InventoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private FoodAdapter adapter;
    private View llEmptyState;
    private List<FoodItem> allFoodItems = new ArrayList<>();
    private String currentCategory = "All";
    private String currentSearchQuery = "";

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
        
        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_inventory);
        llEmptyState = view.findViewById(R.id.ll_inventory_empty_state);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FoodAdapter(new FoodAdapter.onItemClickListener() {
            @Override
            public void onEditClick(FoodItem foodItem) {
                AddItemFragment fragment = new AddItemFragment();
                Bundle args = new Bundle();
                args.putSerializable("foodItem", (Serializable) foodItem);
                fragment.setArguments(args);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView2, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDeleteClick(FoodItem foodItem) {
                deleteItem(foodItem);
            }
        });
        recyclerView.setAdapter(adapter);

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

        // Fetch items from Database
        loadItems();

        FloatingActionButton fab = view.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView2, new AddItemFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadItems() {
        Context context = getContext();
        if (context == null) return;
        AppDatabase db = AppDatabase.getInstance(context);
        Executors.newSingleThreadExecutor().execute(() -> {
            List<FoodItem> items = db.foodItemDao().getAllItems();
            allFoodItems = items;
            
            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(this::filterItems);
            }
        });
    }

    private void filterItems() {
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
        
        adapter.setFoodList(filteredList);

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
    }

    private void deleteItem(FoodItem foodItem) {
        Context context = getContext();
        if (context == null) return;
        AppDatabase db = AppDatabase.getInstance(context);
        Executors.newSingleThreadExecutor().execute(() -> {
            db.foodItemDao().delete(foodItem);
            loadItems();
        });
    }
}
