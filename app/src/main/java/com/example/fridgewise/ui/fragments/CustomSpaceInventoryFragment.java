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
import com.google.android.material.progressindicator.LinearProgressIndicator;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class CustomSpaceInventoryFragment extends Fragment {

    private static final String ARG_SPACE = "space";
    private CustomSpace currentSpace;
    private CustomSpaceViewModel viewModel;
    private RecyclerView recyclerView;
    private CustomSpaceItemAdapter adapter;
    private List<CustomSpaceItem> allItems = new ArrayList<>();
    
    private TextView tvBannerMsg, tvProgressPercent, tvSelectionCount;
    private LinearProgressIndicator progressOverall;
    private CheckBox cbSelectAll;
    private View layoutBanner, layoutEmptyState, layoutStandardHeader, layoutSelectionHeader;

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
        viewModel = new ViewModelProvider(this).get(CustomSpaceViewModel.class);
        if (currentSpace != null) {
            viewModel.setSpaceId(currentSpace.getId());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_custom_space_inventory, container, false);

        tvBannerMsg = view.findViewById(R.id.tvBannerMsg);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        progressOverall = view.findViewById(R.id.progressOverall);
        layoutBanner = view.findViewById(R.id.layoutBanner);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        layoutStandardHeader = view.findViewById(R.id.layoutStandardHeader);
        layoutSelectionHeader = view.findViewById(R.id.layoutSelectionHeader);
        tvSelectionCount = view.findViewById(R.id.tvSelectionCount);
        cbSelectAll = view.findViewById(R.id.cbSelectAll);
        TextView tvTitle = view.findViewById(R.id.tvSpaceTitle);

        if (currentSpace == null) {
            tvTitle.setText(R.string.unknown_space);
            return view;
        }

        tvTitle.setText(currentSpace.getName());
        
        // Professional Polish: Set banner color based on space theme
        if (currentSpace.getColorCode() != 0) {
            layoutBanner.setBackgroundTintList(ColorStateList.valueOf(currentSpace.getColorCode()));
            layoutBanner.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
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
                if (Boolean.TRUE.equals(viewModel.getIsSelectionMode().getValue())) {
                    viewModel.toggleSelection(item.getId());
                } else {
                    Bundle args = new Bundle();
                    args.putInt("arg_space_id", currentSpace.getId());
                    args.putSerializable("arg_item", item);
                    if (getView() != null) {
                        Navigation.findNavController(getView()).navigate(R.id.addSpaceItemFragment, args);
                    }
                }
            }

            @Override
            public void onLongClick(CustomSpaceItem item) {
                if (!Boolean.TRUE.equals(viewModel.getIsSelectionMode().getValue())) {
                    provideHapticFeedback();
                    viewModel.enterSelectionMode(item.getId());
                }
            }

            @Override
            public void onDeleteClick(CustomSpaceItem item) {
                deleteItem(item, -1);
            }

            @Override
            public void onCheckChanged(CustomSpaceItem item, boolean isChecked) {
                item.setChecked(isChecked);
                if (isChecked) {
                    item.setCompletionTimestamp(System.currentTimeMillis());
                    provideHapticFeedback();
                } else {
                    item.setCompletionTimestamp(null);
                }
                viewModel.updateItem(item);
            }

            @Override
            public void onEyeClick(CustomSpaceItem item) {
                NoteDetailBottomSheet sheet = NoteDetailBottomSheet.newInstance(item.getName(), item.getNotes());
                sheet.show(getChildFragmentManager(), "note_detail");
            }
        });
        recyclerView.setAdapter(adapter);

        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            allItems = items;
            adapter.setItems(items, currentSpace);
            updateBannerProgress();
            toggleEmptyState();
        });

        viewModel.getIsSelectionMode().observe(getViewLifecycleOwner(), isSelectionMode -> {
            layoutStandardHeader.setVisibility(isSelectionMode ? View.GONE : View.VISIBLE);
            layoutSelectionHeader.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
            adapter.setSelectionState(isSelectionMode, viewModel.getSelectedIds().getValue());
            if (!isSelectionMode) {
                cbSelectAll.setChecked(false);
            }
        });

        viewModel.getSelectedIds().observe(getViewLifecycleOwner(), selectedIds -> {
            int count = selectedIds.size();
            if (count > 0 && count == allItems.size()) {
                tvSelectionCount.setText(R.string.all_selected);
                cbSelectAll.setChecked(true);
            } else {
                tvSelectionCount.setText(getString(R.string.items_selected, count));
                cbSelectAll.setChecked(false);
            }
            adapter.setSelectionState(Boolean.TRUE.equals(viewModel.getIsSelectionMode().getValue()), selectedIds);
        });

        cbSelectAll.setOnClickListener(v -> {
            if (cbSelectAll.isChecked()) {
                viewModel.selectAll(allItems);
            } else {
                viewModel.deselectAll();
            }
        });

        viewModel.getCurrentSpace().observe(getViewLifecycleOwner(), space -> {
            if (space != null) {
                currentSpace = space;
                tvTitle.setText(currentSpace.getName());
                viewModel.autoRemoveExpiredItems(currentSpace.getAutoRemoveDuration());
            }
        });

        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        view.findViewById(R.id.btnCloseSelection).setOnClickListener(v -> viewModel.exitSelectionMode());
        view.findViewById(R.id.btnDeleteSelected).setOnClickListener(v -> showBulkDeleteConfirmation());
        view.findViewById(R.id.btnShareSelected).setOnClickListener(v -> bulkShare());
        view.findViewById(R.id.btnMoreOptions).setOnClickListener(this::showMoreOptions);

        view.findViewById(R.id.fabAddItem).setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("arg_space_id", currentSpace.getId());
            Navigation.findNavController(v).navigate(R.id.addSpaceItemFragment, args);
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

        setupSwipeToDelete();

        return view;
    }

    private void toggleEmptyState() {
        if (allItems.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            
            ImageView ivEmpty = layoutEmptyState.findViewById(R.id.ivEmptyIllustration);
            if (ivEmpty != null) {
                ivEmpty.setImageResource(currentSpace.getIconResId());
                if (currentSpace.getColorCode() != 0) {
                    ivEmpty.setImageTintList(ColorStateList.valueOf(currentSpace.getColorCode()));
                }
            }
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position < allItems.size()) {
                    CustomSpaceItem item = allItems.get(position);
                    deleteItem(item);
                }
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (Boolean.TRUE.equals(viewModel.getIsSelectionMode().getValue())) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void showBulkDeleteConfirmation() {
        int count = viewModel.getSelectedIds().getValue() != null ? viewModel.getSelectedIds().getValue().size() : 0;
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_items_title, count))
                .setMessage(R.string.bulk_delete_message)
                .setPositiveButton(R.string.bulk_delete_confirm, (dialog, which) -> {
                    viewModel.deleteSelectedItems();
                    Toast.makeText(getContext(), count + " items deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.outline_delete_24)
                .show();
    }

    private void bulkShare() {
        Set<Integer> selectedIds = viewModel.getSelectedIds().getValue();
        if (selectedIds == null || selectedIds.isEmpty()) return;

        List<CustomSpaceItem> selectedItems = new ArrayList<>();
        for (CustomSpaceItem item : allItems) {
            if (selectedIds.contains(item.getId())) {
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("📂 Items from Space: ").append(currentSpace.getName()).append("\n\n");
        for (int i = 0; i < selectedItems.size(); i++) {
            CustomSpaceItem item = selectedItems.get(i);
            sb.append(i + 1).append(". ").append(item.getName());
            sb.append(" (").append(item.getQuantity()).append(" ").append(item.getUnit()).append(")");
            if (item.getDate() != null && !item.getDate().isEmpty()) {
                sb.append(" - Date: ").append(item.getDate());
            }
            if (item.isChecked()) {
                sb.append(" ✅");
            }
            sb.append("\n");
            if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                sb.append("   - ").append(item.getNotes()).append("\n");
            }
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share space items via");
        startActivity(shareIntent);
    }

    private void provideHapticFeedback() {
        Vibrator v = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(50);
            }
        }
    }

    private void updateBannerProgress() {
        if (!currentSpace.isHasCheckbox()) {
            return;
        }

        if (allItems.isEmpty()) {
            progressOverall.setProgress(0);
            tvProgressPercent.setText("0% Completed");
            tvBannerMsg.setText(String.format("Add some items to your %s!", currentSpace.getName()));
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
            Toast.makeText(getContext(), "🎉 All tasks completed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMoreOptions(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add("Edit Space");
        popup.getMenu().add("Delete Space");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle() != null ? item.getTitle().toString() : "";
            if ("Edit Space".equals(title)) {
                Bundle args = new Bundle();
                args.putSerializable("custom_space", currentSpace);
                Navigation.findNavController(v).navigate(R.id.createSpaceFragment, args);
            } else if ("Delete Space".equals(title)) {
                deleteSpace();
            }
            return true;
        });
        popup.show();
    }

    private void deleteSpace() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Space")
                .setMessage("Are you sure you want to delete this entire space? All items inside will be lost.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteSpace(currentSpace);
                    Toast.makeText(getContext(), "Space deleted", Toast.LENGTH_SHORT).show();
                    if (getView() != null) {
                        Navigation.findNavController(getView()).popBackStack();
                    }
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
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteItem(item);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
