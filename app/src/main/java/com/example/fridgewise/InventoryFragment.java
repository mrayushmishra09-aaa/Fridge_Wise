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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Executors;

public class InventoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private FoodAdapter adapter;

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
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FoodAdapter(new FoodAdapter.onItemClickListener() {
            @Override
            public void onEditClick(FoodItem foodItem) {
                // Example: Navigate to AddItemFragment and pass the item to edit
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
            
            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(() -> {
                    adapter.setFoodList(items);
                });
            }
        });
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
