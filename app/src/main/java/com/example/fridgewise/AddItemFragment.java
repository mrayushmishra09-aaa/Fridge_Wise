package com.example.fridgewise;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.concurrent.Executors;

public class AddItemFragment extends Fragment {

    private ImageView img_01;
    private FoodItem editingItem = null; // Track if we are editing

    public AddItemFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_item, container, false);

        // --- View Initializations ---
        img_01 = view.findViewById(R.id.back_arrow);
        TextInputEditText itemNameEditText = view.findViewById(R.id.itemNameEditText);
        TextInputEditText quantityEditText = view.findViewById(R.id.quantityEditText);
        AutoCompleteTextView categoryDropdown = view.findViewById(R.id.categoryDropdown);
        Spinner quantitySpinner = view.findViewById(R.id.spinner_units);
        TextView purchaseDateText = view.findViewById(R.id.purchaseDateText);
        ImageView purchaseCalendarIcon = view.findViewById(R.id.purchaseCalendarIcon);
        TextView expiry_DateText = view.findViewById(R.id.expiry_DateText);
        ImageView expiry_DateIcon = view.findViewById(R.id.expiry_DateIcon);
        Button saveButton = view.findViewById(R.id.save_button);

        // --- Check for Edit Mode ---
        if (getArguments() != null && getArguments().containsKey("foodItem")) {
            editingItem = (FoodItem) getArguments().getSerializable("foodItem");
            if (editingItem != null) {
                // Populate fields with existing data
                itemNameEditText.setText(editingItem.getName());
                quantityEditText.setText(String.valueOf(editingItem.getQuantity()));
                categoryDropdown.setText(editingItem.getCategory());
                purchaseDateText.setText(editingItem.getPurchaseDate());
                expiry_DateText.setText(editingItem.getExpiryDate());
                saveButton.setText("Update Item");
            }
        }

        // --- Default Date if not editing ---
        if (editingItem == null) {
            Calendar c = Calendar.getInstance();
            String today = c.get(Calendar.DAY_OF_MONTH) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.YEAR);
            purchaseDateText.setText(today);
        }

        // --- Setup Dropdowns/Spinners ---
        String[] categories = getResources().getStringArray(R.array.category_array);
        categoryDropdown.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories));
        categoryDropdown.setOnClickListener(v -> categoryDropdown.showDropDown());

        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.quantity_units, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quantitySpinner.setAdapter(unitAdapter);
        
        // Select correct unit if editing
        if (editingItem != null) {
            int spinnerPosition = unitAdapter.getPosition(editingItem.getUnit());
            quantitySpinner.setSelection(spinnerPosition);
        }

        // --- Date Pickers ---
        View.OnClickListener datePickerListener = v -> {
            boolean isPurchase = v.getId() == R.id.purchaseCalendarIcon;
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view1, year, month, day) -> {
                String date = day + "/" + (month + 1) + "/" + year;
                if (isPurchase) purchaseDateText.setText(date);
                else expiry_DateText.setText(date);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        };
        purchaseCalendarIcon.setOnClickListener(datePickerListener);
        expiry_DateIcon.setOnClickListener(datePickerListener);

        if (img_01 != null) img_01.setOnClickListener(v -> requireActivity().onBackPressed());

        // --- Save / Update Logic ---
        saveButton.setOnClickListener(v -> {
            String itemName = itemNameEditText.getText().toString().trim();
            String quantityStr = quantityEditText.getText().toString().trim();
            String category = categoryDropdown.getText().toString().trim();
            String unit = quantitySpinner.getSelectedItem().toString();
            String purchaseDate = purchaseDateText.getText().toString().trim();
            String expiryDate = expiry_DateText.getText().toString().trim();

            if (itemName.isEmpty() || quantityStr.isEmpty() || category.isEmpty() || expiryDate.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double quantity = Double.parseDouble(quantityStr);

            Context context = getContext();
            if (context == null) return;
            AppDatabase database = AppDatabase.getInstance(context);
            Executors.newSingleThreadExecutor().execute(() -> {
                if (editingItem == null) {
                    // INSERT new item
                    FoodItem newItem = new FoodItem(itemName, quantity, unit, category, purchaseDate, expiryDate);
                    database.foodItemDao().insert(newItem);
                } else {
                    // UPDATE existing item
                    editingItem.setName(itemName);
                    editingItem.setQuantity(quantity);
                    editingItem.setUnit(unit);
                    editingItem.setCategory(category);
                    editingItem.setPurchaseDate(purchaseDate);
                    editingItem.setExpiryDate(expiryDate);
                    database.foodItemDao().update(editingItem);
                }

                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        Toast.makeText(context, editingItem == null ? "Item added" : "Item updated", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    });
                }
            });
        });

        return view;
    }
}
