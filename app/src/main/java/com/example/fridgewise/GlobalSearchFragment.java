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
import java.util.stream.Collectors;

public class GlobalSearchFragment extends Fragment {

    private RecyclerView rvFood, rvMed, rvDoc, rvShopping, rvTodo;
    private FoodAdapter foodAdapter;
    private MedicineAdapter medAdapter;
    private DocumentAdapter docAdapter;
    private ShoppingAdapter shoppingAdapter;
    private TodoAdapter todoAdapter;
    private SearchView searchView;
    
    private TextView tvFoodHeader, tvMedHeader, tvDocHeader, tvShoppingHeader, tvTodoHeader, tvEmptyMessage;
    private View llEmptyState;
    
    private List<FoodItem> allFood = new ArrayList<>();
    private List<MedicineEntity> allMeds = new ArrayList<>();
    private List<DocumentItem> allDocs = new ArrayList<>();
    private List<ShoppingItem> allShopping = new ArrayList<>();
    private List<TodoItem> allTodos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_global_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Header Views
        tvFoodHeader = view.findViewById(R.id.tvFoodHeader);
        tvMedHeader = view.findViewById(R.id.tvMedHeader);
        tvDocHeader = view.findViewById(R.id.tvDocHeader);
        tvShoppingHeader = view.findViewById(R.id.tvShoppingHeader);
        tvTodoHeader = view.findViewById(R.id.tvTodoHeader);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        llEmptyState = view.findViewById(R.id.llEmptyState);

        // RecyclerViews
        rvFood = view.findViewById(R.id.rvFoodResults);
        rvMed = view.findViewById(R.id.rvMedResults);
        rvDoc = view.findViewById(R.id.rvDocResults);
        rvShopping = view.findViewById(R.id.rvShoppingResults);
        rvTodo = view.findViewById(R.id.rvTodoResults);

        setupAdapters();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });

        if (getArguments() != null) {
            String initialQuery = getArguments().getString("search_query", "");
            if (!initialQuery.isEmpty()) {
                searchView.setQuery(initialQuery, true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllData();
    }

    private void setupAdapters() {
        // Food Adapter
        foodAdapter = new FoodAdapter(new FoodAdapter.onItemClickListener() {
            @Override
            public void onEditClick(FoodItem foodItem) {
                navigateToEdit(foodItem);
            }

            @Override
            public void onDeleteClick(FoodItem foodItem) {
                navigateToEdit(foodItem);
            }

            @Override
            public void onInfoClick(FoodItem foodItem) {
                FoodInfoBottomSheet sheet = FoodInfoBottomSheet.newInstance(foodItem);
                sheet.show(getChildFragmentManager(), "food_info");
            }
        });
        rvFood.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFood.setAdapter(foodAdapter);

        // Medicine Adapter
        medAdapter = new MedicineAdapter(new ArrayList<>(), new MedicineAdapter.OnMedicineClickListener() {
            @Override
            public void onReminderToggle(MedicineEntity medicine, boolean isChecked) {
                medicine.setReminderOn(isChecked);
                updateMedicine(medicine);
            }

            @Override
            public void onTakeDose(MedicineEntity medicine) {
            }

            @Override
            public void onEditClick(MedicineEntity medicine) {
                navigateToEditMed(medicine);
            }

            @Override
            public void onDeleteClick(MedicineEntity medicine) {
                navigateToEditMed(medicine);
            }
        });
        rvMed.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMed.setAdapter(medAdapter);

        // Document Adapter
        docAdapter = new DocumentAdapter(new DocumentAdapter.OnDocumentClickListener() {
            @Override
            public void onEditClick(DocumentItem document) {
                navigateToEditDoc(document);
            }

            @Override
            public void onDeleteClick(DocumentItem document) {
                navigateToEditDoc(document);
            }

            @Override
            public void onDownloadClick(DocumentItem document) {
            }
        });
        rvDoc.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDoc.setAdapter(docAdapter);

        // Shopping Adapter
        shoppingAdapter = new ShoppingAdapter();
        shoppingAdapter.setOnItemClickListener(new ShoppingAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(ShoppingItem item) {
                replaceFragment(new ShoppingListFragment());
            }

            @Override
            public void onDeleteClick(ShoppingItem item) {
                replaceFragment(new ShoppingListFragment());
            }

            @Override
            public void onStatusChange(ShoppingItem item, boolean isCompleted) {
                updateShopping(item);
            }
        });
        rvShopping.setLayoutManager(new LinearLayoutManager(getContext()));
        rvShopping.setAdapter(shoppingAdapter);

        // Todo Adapter
        todoAdapter = new TodoAdapter(new ArrayList<>());
        todoAdapter.setOnTodoItemClickListener(new TodoAdapter.OnTodoItemClickListener() {
            @Override
            public void onEditClick(TodoItem item) {
                replaceFragment(new TodoListFragment());
            }

            @Override
            public void onDeleteClick(TodoItem item) {
                replaceFragment(new TodoListFragment());
            }

            @Override
            public void onStatusChange(TodoItem item, boolean isCompleted) {
                updateTodo(item, isCompleted);
            }
        });
        rvTodo.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTodo.setAdapter(todoAdapter);
    }

    private void loadAllData() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            allFood = db.foodItemDao().getAllItems();
            allMeds = db.medicineDao().getAllMedicines();
            allDocs = db.documentDao().getAllDocuments();
            allShopping = db.shoppingDao().getAllItems();
            allTodos = db.todoDao().getAllTodos();

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (searchView != null) {
                        performSearch(searchView.getQuery().toString());
                    }
                });
            }
        });
    }

    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            hideAll();
            llEmptyState.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText(R.string.type_to_find_anything);
            return;
        }

        String q = query.toLowerCase().trim();

        List<FoodItem> filteredFood = allFood.stream()
                .filter(i -> i.getName().toLowerCase().contains(q) || i.getCategory().toLowerCase().contains(q))
                .collect(Collectors.toList());

        List<MedicineEntity> filteredMeds = allMeds.stream()
                .filter(i -> i.getMedicineName().toLowerCase().contains(q) || i.getMedicineType().toLowerCase().contains(q))
                .collect(Collectors.toList());

        List<DocumentItem> filteredDocs = allDocs.stream()
                .filter(i -> i.getName().toLowerCase().contains(q) || i.getCategory().toLowerCase().contains(q))
                .collect(Collectors.toList());

        List<ShoppingItem> filteredShopping = allShopping.stream()
                .filter(i -> i.getName().toLowerCase().contains(q))
                .collect(Collectors.toList());

        List<TodoItem> filteredTodos = allTodos.stream()
                .filter(i -> i.getTitle().toLowerCase().contains(q) || i.getNote().toLowerCase().contains(q))
                .collect(Collectors.toList());

        updateUI(filteredFood, filteredMeds, filteredDocs, filteredShopping, filteredTodos);
    }

    private void updateUI(List<FoodItem> food, List<MedicineEntity> meds, List<DocumentItem> docs, List<ShoppingItem> shopping, List<TodoItem> todos) {
        boolean hasResults = !food.isEmpty() || !meds.isEmpty() || !docs.isEmpty() || !shopping.isEmpty() || !todos.isEmpty();

        tvFoodHeader.setVisibility(food.isEmpty() ? View.GONE : View.VISIBLE);
        rvFood.setVisibility(food.isEmpty() ? View.GONE : View.VISIBLE);
        foodAdapter.setFoodList(food);

        tvMedHeader.setVisibility(meds.isEmpty() ? View.GONE : View.VISIBLE);
        rvMed.setVisibility(meds.isEmpty() ? View.GONE : View.VISIBLE);
        medAdapter.updateList(meds);

        tvDocHeader.setVisibility(docs.isEmpty() ? View.GONE : View.VISIBLE);
        rvDoc.setVisibility(docs.isEmpty() ? View.GONE : View.VISIBLE);
        docAdapter.setDocs(docs);

        tvShoppingHeader.setVisibility(shopping.isEmpty() ? View.GONE : View.VISIBLE);
        rvShopping.setVisibility(shopping.isEmpty() ? View.GONE : View.VISIBLE);
        shoppingAdapter.setItems(shopping);

        tvTodoHeader.setVisibility(todos.isEmpty() ? View.GONE : View.VISIBLE);
        rvTodo.setVisibility(todos.isEmpty() ? View.GONE : View.VISIBLE);
        todoAdapter.updateList(todos);

        llEmptyState.setVisibility(hasResults ? View.GONE : View.VISIBLE);
        if (!hasResults) {
            tvEmptyMessage.setText(R.string.no_results_found);
        }
    }

    private void hideAll() {
        tvFoodHeader.setVisibility(View.GONE);
        rvFood.setVisibility(View.GONE);
        tvMedHeader.setVisibility(View.GONE);
        rvMed.setVisibility(View.GONE);
        tvDocHeader.setVisibility(View.GONE);
        rvDoc.setVisibility(View.GONE);
        tvShoppingHeader.setVisibility(View.GONE);
        rvShopping.setVisibility(View.GONE);
        tvTodoHeader.setVisibility(View.GONE);
        rvTodo.setVisibility(View.GONE);
    }

    private void updateMedicine(MedicineEntity medicine) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(requireContext()).medicineDao().update(medicine);
        });
    }

    private void updateShopping(ShoppingItem item) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(requireContext()).shoppingDao().update(item);
        });
    }

    private void updateTodo(TodoItem item, boolean isDone) {
        item.setCompleted(isDone);
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(requireContext()).todoDao().update(item);
        });
    }

    private void navigateToEdit(FoodItem item) {
        AddItemFragment fragment = new AddItemFragment();
        Bundle args = new Bundle();
        args.putSerializable("foodItem", item);
        fragment.setArguments(args);
        replaceFragment(fragment);
    }

    private void navigateToEditMed(MedicineEntity med) {
        MedicineAddFragment fragment = new MedicineAddFragment();
        Bundle args = new Bundle();
        args.putSerializable(MedicineAddFragment.ARG_MEDICINE, med);
        fragment.setArguments(args);
        replaceFragment(fragment);
    }

    private void navigateToEditDoc(DocumentItem doc) {
        AddDocumentFragment fragment = new AddDocumentFragment();
        Bundle args = new Bundle();
        args.putSerializable("document", doc);
        fragment.setArguments(args);
        replaceFragment(fragment);
    }

    private void replaceFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragmentContainerView2, fragment)
                .addToBackStack(null)
                .commit();
    }
}
