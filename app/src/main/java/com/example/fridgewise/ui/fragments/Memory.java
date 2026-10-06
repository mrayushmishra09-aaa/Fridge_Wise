package com.example.fridgewise.ui.fragments;

import com.example.fridgewise.R;
import com.example.fridgewise.data.*;
import com.example.fridgewise.model.*;
import com.example.fridgewise.adapter.*;
import com.example.fridgewise.util.*;
import com.example.fridgewise.util.SecurityManager;
import com.example.fridgewise.ui.viewmodel.*;
import com.example.fridgewise.ui.activities.*;
import com.example.fridgewise.ui.bottomsheet.*;
import com.example.fridgewise.ui.dialog.*;

import android.content.Context;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Memory extends Fragment {

    private MaterialCardView cardTodo;
    private RecyclerView rvCustomSpaces;
    private CustomSpaceAdapter customSpaceAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private boolean isHiddenSpacesRevealed = false;

    public Memory() {
        // Required empty public constructor
    }

    public static Memory newInstance() {
        return new Memory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memory, container, false);

        CardView cardMedicine = view.findViewById(R.id.cardMedicine);
        cardMedicine.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.med_section));

        cardTodo = view.findViewById(R.id.cardTodo);
        cardTodo.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.todoListFragment));

        CardView cardShopping = view.findViewById(R.id.cardShopping);
        cardShopping.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.shoppingListFragment));

        CardView cardDocs = view.findViewById(R.id.cardDocs);
        cardDocs.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.documentListFragment));

        rvCustomSpaces = view.findViewById(R.id.rvCustomSpaces);
        rvCustomSpaces.setLayoutManager(new LinearLayoutManager(getContext()));
        customSpaceAdapter = new CustomSpaceAdapter(new CustomSpaceAdapter.OnSpaceClickListener() {
            @Override
            public void onSpaceClick(CustomSpace space) {
                openSpaceWithSecurity(space, view);
            }

            @Override
            public void onSpaceLongClick(CustomSpace space, View view) {
                showSpaceOptions(space, view);
            }
        });
        rvCustomSpaces.setAdapter(customSpaceAdapter);

        View addCollectionBtn = view.findViewById(R.id.addCollectionBtn);
        addCollectionBtn.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.createSpaceFragment));

        updateCounts(view);
        loadCustomSpaces();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            updateCounts(getView());
            loadCustomSpaces();
        }
    }

    private void openSpaceWithSecurity(CustomSpace space, View view) {
        SecurityManager sec = SecurityManager.getInstance(requireContext());
        if (space.isProtected() && !sec.isSpaceUnlockedInSession(space.getId())) {
            SecurityAuthDialog authDialog = SecurityAuthDialog.newInstance(space, new SecurityAuthDialog.AuthCallback() {
                @Override
                public void onAuthenticated() {
                    sec.unlockSpaceSession(space.getId());
                    navigateToSpace(space, view);
                }

                @Override
                public void onCanceled() {}
            });
            authDialog.show(getParentFragmentManager(), "auth_space_dialog");
        } else {
            navigateToSpace(space, view);
        }
    }

    private void navigateToSpace(CustomSpace space, View view) {
        Bundle args = new Bundle();
        args.putSerializable("space", space);
        Navigation.findNavController(view).navigate(R.id.customSpaceInventoryFragment, args);
    }

    private void loadCustomSpaces() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<CustomSpace> allSpaces = db.customSpaceDao().getAllSpacesSync();
            
            List<CustomSpace> displaySpaces = new ArrayList<>();
            SparseIntArray counts = new SparseIntArray();

            for (CustomSpace space : allSpaces) {
                if (space.isHidden() && !isHiddenSpacesRevealed) {
                    continue; // Filter out hidden spaces unless explicitly revealed
                }
                displaySpaces.add(space);
                int count = db.customSpaceDao().getItemCountForSpace(space.getId());
                counts.put(space.getId(), count);
            }
            
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    customSpaceAdapter.setSpaces(displaySpaces, counts);
                });
            }
        });
    }

    private void showSpaceOptions(CustomSpace space, View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add("Edit");
        popup.getMenu().add(space.isProtected() ? "Unprotect Space 🔓" : "Protect Space 🔒");
        popup.getMenu().add(space.isHidden() ? "Unhide Space 👁" : "Hide Space 👁");
        popup.getMenu().add("Delete");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle() != null ? item.getTitle().toString() : "";
            if ("Edit".equals(title)) {
                Bundle args = new Bundle();
                args.putSerializable("custom_space", space);
                Navigation.findNavController(v).navigate(R.id.createSpaceFragment, args);
            } else if (title.startsWith("Unprotect")) {
                space.setProtected(false);
                space.setProtectionType("NONE");
                updateSpaceInDb(space, "Space unprotected");
            } else if (title.startsWith("Protect")) {
                SecurityManager sec = SecurityManager.getInstance(requireContext());
                if (!sec.isMasterLockEnabled()) {
                    SetupPinDialog dialog = SetupPinDialog.newInstance("Set Master PIN to Protect Space", new SetupPinDialog.SetupPinCallback() {
                        @Override
                        public void onPinCreated(String pin, String salt, String hash) {
                            sec.setupMasterPin(pin);
                            space.setProtected(true);
                            space.setProtectionType("MASTER_PIN");
                            updateSpaceInDb(space, "Space protected");
                        }

                        @Override
                        public void onCanceled() {}
                    });
                    dialog.show(getParentFragmentManager(), "setup_pin_dialog");
                } else {
                    space.setProtected(true);
                    space.setProtectionType("MASTER_PIN");
                    updateSpaceInDb(space, "Space protected");
                }
            } else if (title.startsWith("Unhide")) {
                space.setHidden(false);
                updateSpaceInDb(space, "Space unhidden");
            } else if (title.startsWith("Hide")) {
                space.setHidden(true);
                updateSpaceInDb(space, "Space hidden");
            } else if ("Delete".equals(title)) {
                if (space.isProtected()) {
                    SecurityAuthDialog authDialog = SecurityAuthDialog.newInstance(space, new SecurityAuthDialog.AuthCallback() {
                        @Override
                        public void onAuthenticated() {
                            deleteSpace(space);
                        }

                        @Override
                        public void onCanceled() {}
                    });
                    authDialog.show(getParentFragmentManager(), "auth_delete_space");
                } else {
                    deleteSpace(space);
                }
            }
            return true;
        });
        popup.show();
    }

    private void updateSpaceInDb(CustomSpace space, String message) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            db.customSpaceDao().updateSpace(space);
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    loadCustomSpaces();
                });
            }
        });
    }

    private void deleteSpace(CustomSpace space) {
        executor.execute(() -> {
            Context context = requireContext();
            AppDatabase db = AppDatabase.getInstance(context);
            List<CustomSpaceItem> items = db.customSpaceDao().getItemsForSpaceSync(space.getId());
            for (CustomSpaceItem item : items) {
                db.customSpaceDao().deleteItem(item);
                int notificationId = NotificationHelper.generateId("SPACE", item.getId());
                NotificationHelper.cancelNotification(context, notificationId);
            }
            db.customSpaceDao().deleteSpace(space);
            
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Space deleted", Toast.LENGTH_SHORT).show();
                    loadCustomSpaces();
                });
            }
        });
    }

    private void updateCounts(View view) {
        if (view == null) return;
        
        TextView tvMedicineCount = view.findViewById(R.id.tvMedicineCount);
        TextView tvTodoCount = view.findViewById(R.id.tvTodoCount);
        TextView tvShoppingCount = view.findViewById(R.id.tvShoppingCount);
        TextView tvDocsCount = view.findViewById(R.id.tvDocsCount);

        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            int medCount = db.medicineDao().getAllMedicines().size();
            int todoCount = db.todoDao().getAllTodos().size();
            int shoppingCount = db.shoppingDao().getAllItems().size();
            int docsCount = db.documentDao().getAllDocuments().size();

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (tvMedicineCount != null) tvMedicineCount.setText(medCount + (medCount == 1 ? " reminder" : " reminders"));
                    if (tvTodoCount != null) tvTodoCount.setText(todoCount + (todoCount == 1 ? " task" : " tasks"));
                    if (tvShoppingCount != null) {
                        tvShoppingCount.setText(shoppingCount + (shoppingCount == 1 ? " item" : " items"));
                    }
                    if (tvDocsCount != null) {
                        tvDocsCount.setText(docsCount + (docsCount == 1 ? " document" : " documents"));
                    }
                });
            }
        });
    }
}
