package com.example.fridgewise;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
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

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class AddItemFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ImageView img_01;

    public AddItemFragment() {
        // Required empty public constructor
    }

    public static AddItemFragment newInstance(String param1, String param2) {
        AddItemFragment fragment = new AddItemFragment();
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

        // --- SET DEFAULT DATES ---
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Set Purchase Date to Today by default
        String today = day + "/" + (month + 1) + "/" + year;
        purchaseDateText.setText(today);

        // Ensure Expiry Date is empty initially
        expiry_DateText.setText("");

        // --- Back Arrow Setup ---
        if (img_01 != null) {
            img_01.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        // --- Category Dropdown Setup ---
        String[] categories = getResources().getStringArray(R.array.category_array);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(), 
                android.R.layout.simple_dropdown_item_1line, 
                categories
        );
        categoryDropdown.setAdapter(categoryAdapter);
        categoryDropdown.setOnClickListener(v -> categoryDropdown.showDropDown());
        
        // --- Quantity Units Spinner Setup ---
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.quantity_units, 
                android.R.layout.simple_spinner_item
        );
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quantitySpinner.setAdapter(unitAdapter);

        // --- Date Picker: Purchase Date ---
        purchaseCalendarIcon.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int pYear = calendar.get(Calendar.YEAR);
            int pMonth = calendar.get(Calendar.MONTH);
            int pDay = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, yearSelected, monthOfYear, dayOfMonth) -> {
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + yearSelected;
                        purchaseDateText.setText(selectedDate);
                        purchaseDateText.setError(null); // Clear error when date is picked
                    }, pYear, pMonth, pDay);
            datePickerDialog.show();
        });
        
        // --- Date Picker: Expiry Date ---
        expiry_DateIcon.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int eYear = calendar.get(Calendar.YEAR);
            int eMonth = calendar.get(Calendar.MONTH);
            int eDay = calendar.get(Calendar.DAY_OF_MONTH);
            
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, yearSelected, monthOfYear, dayOfMonth) -> {
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + yearSelected;
                        expiry_DateText.setText(selectedDate);
                        expiry_DateText.setError(null); // Clear error when date is picked
                    }, eYear, eMonth, eDay);
            datePickerDialog.show();
        });

        // --- Save Button & Validation Logic ---
        saveButton.setOnClickListener(v -> {
            String itemName = itemNameEditText.getText().toString().trim();
            String quantity = quantityEditText.getText().toString().trim();
            String category = categoryDropdown.getText().toString().trim();
            String purchaseDate = purchaseDateText.getText().toString().trim();
            String expiryDate = expiry_DateText.getText().toString().trim();

            // Clear previous errors
            itemNameEditText.setError(null);
            quantityEditText.setError(null);
            categoryDropdown.setError(null);
            purchaseDateText.setError(null);
            expiry_DateText.setError(null);

            if (itemName.isEmpty()) {
                itemNameEditText.setError("Item name is required");
                itemNameEditText.requestFocus();
                return;
            }

            if (category.isEmpty()) {
                categoryDropdown.setError("Please select a category");
                categoryDropdown.requestFocus();
                return;
            }

            if (quantity.isEmpty()) {
                quantityEditText.setError("Quantity is required");
                quantityEditText.requestFocus();
                return;
            }

            if (purchaseDate.isEmpty() || purchaseDate.equals("Select Date")) {
                purchaseDateText.setError("Purchase date is required");
                purchaseDateText.requestFocus();
                return;
            }

            if (expiryDate.isEmpty()) {
                expiry_DateText.setError("Expiry date is required");
                expiry_DateText.requestFocus();
                return;
            }

            // Success!
            Toast.makeText(requireContext(), "Item added successfully", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
