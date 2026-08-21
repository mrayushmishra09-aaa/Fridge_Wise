package com.example.fridgewise;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Memory#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Memory extends Fragment {

    MaterialCardView cardTodo;
    private RecyclerView rvCustomSpaces;
    private CustomSpaceAdapter customSpaceAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Memory() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment chefassistentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Memory newInstance(String param1, String param2) {
        Memory fragment = new Memory();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_memory, container, false );
        
        // 1. Medicine section click listener
        CardView cardMedicine = view.findViewById(R.id.cardMedicine);
        cardMedicine.setOnClickListener(v ->{
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView2, new Med_section())
                    .addToBackStack(null)
                    .commit();
        });

        // 2. Todo section click listener
        cardTodo = view.findViewById(R.id.cardTodo);
        cardTodo.setOnClickListener(v ->{
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView2, new TodoListFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 3. Shopping List section click listener
        CardView cardShopping = view.findViewById(R.id.cardShopping);
        cardShopping.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView2, new ShoppingListFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 4. Documents section click listener
        CardView cardDocs = view.findViewById(R.id.cardDocs);
        cardDocs.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView2, new DocumentListFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Custom Spaces Setup
        rvCustomSpaces = view.findViewById(R.id.rvCustomSpaces);
        rvCustomSpaces.setLayoutManager(new LinearLayoutManager(getContext()));
        customSpaceAdapter = new CustomSpaceAdapter(new CustomSpaceAdapter.OnSpaceClickListener() {
            @Override
            public void onSpaceClick(CustomSpace space) {
                // Open Custom Space Inventory
                CustomSpaceInventoryFragment fragment = CustomSpaceInventoryFragment.newInstance(space);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView2, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onSpaceLongClick(CustomSpace space, View view) {
                showSpaceOptions(space, view);
            }
        });
        rvCustomSpaces.setAdapter(customSpaceAdapter);

        // Add Collection Button click listener
        View addCollectionBtn = view.findViewById(R.id.addCollectionBtn);
        addCollectionBtn.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView2, CreateSpaceFragment.newInstance(null))
                    .addToBackStack(null)
                    .commit();
        });

        updateCounts(view);
        loadCustomSpaces();

        return view;
    }

    private void loadCustomSpaces() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<CustomSpace> spaces = db.customSpaceDao().getAllSpaces();
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    customSpaceAdapter.setSpaces(spaces);
                });
            }
        });
    }

    private void showSpaceOptions(CustomSpace space, View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add("Edit");
        popup.getMenu().add("Delete");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle() != null ? item.getTitle().toString() : "";
            if ("Edit".equals(title)) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView2, CreateSpaceFragment.newInstance(space))
                        .addToBackStack(null)
                        .commit();
            } else if ("Delete".equals(title)) {
                deleteSpace(space);
            }
            return true;
        });
        popup.show();
    }

    private void deleteSpace(CustomSpace space) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            // Delete all items in the space first
            List<CustomSpaceItem> items = db.customSpaceDao().getItemsForSpace(space.getId());
            for (CustomSpaceItem item : items) {
                db.customSpaceDao().deleteItem(item);
            }
            // Delete the space itself
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
        TextView tvMedicineCount = view.findViewById(R.id.tvMedicineCount);
        TextView tvTodoCount = view.findViewById(R.id.tvTodoCount);
        TextView tvShoppingCount = view.findViewById(R.id.tvShoppingCount);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            int medCount = db.medicineDao().getAllMedicines().size();
            int todoCount = db.todoDao().getAllTodos().size();
            int shoppingCount = db.shoppingDao().getAllItems().size();

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    tvMedicineCount.setText(medCount + (medCount == 1 ? " reminder" : " reminders"));
                    tvTodoCount.setText(todoCount + (todoCount == 1 ? " task" : " tasks"));
                    if (tvShoppingCount != null) {
                        tvShoppingCount.setText(shoppingCount + (shoppingCount == 1 ? " item" : " items"));
                    }
                });
            }
        }).start();
    }
}