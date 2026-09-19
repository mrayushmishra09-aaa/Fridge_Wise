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

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListFragment extends Fragment {

    private RecyclerView rvShoppingList;
    private ShoppingAdapter adapter;
    private List<ShoppingItem> shoppingItems = new ArrayList<>();
    private TextView tvItemCount, tvSelectionCount;
    private EditText etQuickAdd;
    private ImageButton btnQuickAdd, btnBack, btnShare;
    private View selectionToolbar, headerLayout;
    private CheckBox cbSelectAll;
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
        btnShare = view.findViewById(R.id.btnShare);
        fabAdd = view.findViewById(R.id.fabAddShopping);
        llEmptyState = view.findViewById(R.id.ll_shopping_empty_state);
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

        btnShare.setOnClickListener(v -> shareShoppingList());

        fabAdd.setOnClickListener(v -> showAddEditDialog(null));

        return view;
    }

    private void shareShoppingList() {
        if (shoppingItems.isEmpty()) {
            Toast.makeText(getContext(), "List is empty, nothing to share", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🛒 My Shopping List from FridgeWise:\n\n");
        for (int i = 0; i < shoppingItems.size(); i++) {
            ShoppingItem item = shoppingItems.get(i);
            sb.append(i + 1).append(". ").append(item.getName());
            if (item.getQuantity() != null && !item.getQuantity().isEmpty()) {
                sb.append(" (").append(item.getQuantity());
                if (item.getUnit() != null && !item.getUnit().isEmpty()) {
                    sb.append(" ").append(item.getUnit());
                }
                sb.append(")");
            }
            if (item.isCompleted()) {
                sb.append(" ✅");
            }
            sb.append("\n");
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share Shopping List via");
        startActivity(shareIntent);
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

            @Override
            public void onSelectionModeChanged(boolean isSelectionMode) {
                if (isSelectionMode) enterSelectionMode();
                else exitSelectionMode();
            }

            @Override
            public void onSelectionCountChanged(int count) {
                if (tvSelectionCount != null) {
                    if (count > 0 && count == shoppingItems.size()) {
                        tvSelectionCount.setText("All selected");
                        if (cbSelectAll != null) cbSelectAll.setChecked(true);
                    } else {
                        tvSelectionCount.setText(count + " selected");
                        if (cbSelectAll != null) cbSelectAll.setChecked(false);
                    }
                }
            }
        });
        rvShoppingList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvShoppingList.setAdapter(adapter);

        setupSwipeToDelete();
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
                if (position < shoppingItems.size()) {
                    deleteItem(shoppingItems.get(position));
                }
            }
        }).attachToRecyclerView(rvShoppingList);
    }

    private void enterSelectionMode() {
        if (selectionToolbar != null) selectionToolbar.setVisibility(View.VISIBLE);
        if (headerLayout != null) headerLayout.setVisibility(View.GONE);
        if (fabAdd != null) fabAdd.hide();
    }

    private void exitSelectionMode() {
        if (selectionToolbar != null) selectionToolbar.setVisibility(View.GONE);
        if (headerLayout != null) headerLayout.setVisibility(View.VISIBLE);
        if (adapter != null) adapter.clearSelection();
        if (cbSelectAll != null) cbSelectAll.setChecked(false);
        if (fabAdd != null) fabAdd.show();
    }

    private void bulkDelete() {
        List<ShoppingItem> selectedItems = adapter.getSelectedItems();
        if (selectedItems.isEmpty()) return;

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete " + selectedItems.size() + " items?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete Forever", (dialog, which) -> {
                    new Thread(() -> {
                        for (ShoppingItem item : selectedItems) {
                            db.shoppingDao().delete(item);
                        }
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Items deleted", Toast.LENGTH_SHORT).show();
                                exitSelectionMode();
                                loadItems();
                            });
                        }
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void bulkShare() {
        List<ShoppingItem> selectedItems = adapter.getSelectedItems();
        if (selectedItems.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("🛒 Shared Shopping Items from FridgeWise:\n\n");
        for (int i = 0; i < selectedItems.size(); i++) {
            ShoppingItem item = selectedItems.get(i);
            sb.append(i + 1).append(". ").append(item.getName());
            if (item.getQuantity() != null && !item.getQuantity().isEmpty()) {
                sb.append(" (").append(item.getQuantity());
                if (item.getUnit() != null && !item.getUnit().isEmpty()) {
                    sb.append(" ").append(item.getUnit());
                }
                sb.append(")");
            }
            if (item.isCompleted()) {
                sb.append(" ✅");
            }
            sb.append("\n");
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share selected items via");
        startActivity(shareIntent);
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
