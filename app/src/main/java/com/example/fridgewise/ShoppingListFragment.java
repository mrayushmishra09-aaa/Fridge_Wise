package com.example.fridgewise;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListFragment extends Fragment {

    private RecyclerView rvShoppingList;
    private ShoppingAdapter adapter;
    private List<ShoppingItem> shoppingItems = new ArrayList<>();
    private TextView tvItemCount;
    private EditText etQuickAdd;
    private ImageButton btnQuickAdd, btnBack;
    private FloatingActionButton fabAdd;
    private AppDatabase db;
    private View llEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        db = AppDatabase.getInstance(requireContext());
        
        rvShoppingList = view.findViewById(R.id.rvShoppingList);
        tvItemCount = view.findViewById(R.id.tvItemCount);
        etQuickAdd = view.findViewById(R.id.etQuickAdd);
        btnQuickAdd = view.findViewById(R.id.btnQuickAdd);
        btnBack = view.findViewById(R.id.btnBack);
        fabAdd = view.findViewById(R.id.fabAddShopping);
        llEmptyState = view.findViewById(R.id.ll_shopping_empty_state);

        setupRecyclerView();
        loadItems();

        btnQuickAdd.setOnClickListener(v -> {
            String name = etQuickAdd.getText().toString().trim();
            if (!name.isEmpty()) {
                addQuickItem(name);
                etQuickAdd.setText("");
            } else {
                Toast.makeText(getContext(), "Please enter an item name", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        fabAdd.setOnClickListener(v -> showAddEditDialog(null));

        return view;
    }

    private void setupRecyclerView() {
        adapter = new ShoppingAdapter();
        adapter.setOnItemClickListener(new ShoppingAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(ShoppingItem item) {
                showAddEditDialog(item);
            }

            @Override
            public void onDeleteClick(ShoppingItem item) {
                deleteItem(item);
            }

            @Override
            public void onStatusChange(ShoppingItem item, boolean isCompleted) {
                updateItem(item);
            }
        });
        rvShoppingList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvShoppingList.setAdapter(adapter);
    }

    private void loadItems() {
        new Thread(() -> {
            List<ShoppingItem> items = db.shoppingDao().getAllItems();
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    shoppingItems.clear();
                    shoppingItems.addAll(items);
                    adapter.setItems(shoppingItems);
                    updateHeader();
                    
                    if (llEmptyState != null) {
                        if (shoppingItems.isEmpty()) {
                            llEmptyState.setVisibility(View.VISIBLE);
                            rvShoppingList.setVisibility(View.GONE);
                        } else {
                            llEmptyState.setVisibility(View.GONE);
                            rvShoppingList.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }).start();
    }

    private void addQuickItem(String name) {
        ShoppingItem item = new ShoppingItem(name, "1", "", false);
        new Thread(() -> {
            db.shoppingDao().insert(item);
            loadItems();
        }).start();
    }

    private void updateItem(ShoppingItem item) {
        new Thread(() -> {
            db.shoppingDao().update(item);
            loadItems();
        }).start();
    }

    private void deleteItem(ShoppingItem item) {
        new Thread(() -> {
            db.shoppingDao().delete(item);
            loadItems();
        }).start();
    }

    private void updateHeader() {
        int count = shoppingItems.size();
        tvItemCount.setText(count + (count == 1 ? " item in your list" : " items in your list"));
    }

    private void showAddEditDialog(ShoppingItem itemToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_shopping_item, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etItemName);
        EditText etQty = dialogView.findViewById(R.id.etItemQuantity);
        EditText etUnit = dialogView.findViewById(R.id.etItemUnit);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);

        if (itemToEdit != null) {
            tvDialogTitle.setText("Edit Item");
            etName.setText(itemToEdit.getName());
            etQty.setText(itemToEdit.getQuantity());
            etUnit.setText(itemToEdit.getUnit());
        } else {
            tvDialogTitle.setText("Add New Item");
        }

        builder.setPositiveButton(itemToEdit == null ? "Add" : "Update", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String qty = etQty.getText().toString().trim();
            String unit = etUnit.getText().toString().trim();

            if (!name.isEmpty()) {
                if (itemToEdit == null) {
                    ShoppingItem newItem = new ShoppingItem(name, qty, unit, false);
                    new Thread(() -> {
                        db.shoppingDao().insert(newItem);
                        loadItems();
                    }).start();
                } else {
                    itemToEdit.setName(name);
                    itemToEdit.setQuantity(qty);
                    itemToEdit.setUnit(unit);
                    updateItem(itemToEdit);
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }
}
