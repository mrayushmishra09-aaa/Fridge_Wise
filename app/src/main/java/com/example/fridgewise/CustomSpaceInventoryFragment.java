package com.example.fridgewise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
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
        });
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

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
            allItems = db.customSpaceDao().getItemsForSpace(currentSpace.getId());
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> adapter.setItems(allItems));
            }
        });
    }

    private void filterItems(String query) {
        List<CustomSpaceItem> filtered = new ArrayList<>();
        for (CustomSpaceItem item : allItems) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(item);
            }
        }
        adapter.setItems(filtered);
    }

    private void deleteItem(CustomSpaceItem item) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(requireContext()).customSpaceDao().deleteItem(item);
            loadItems();
        });
    }
}
