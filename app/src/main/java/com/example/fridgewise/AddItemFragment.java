package com.example.fridgewise;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

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

        // Set up back arrow
        img_01 = view.findViewById(R.id.back_arrow);
        if (img_01 != null) {
            img_01.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                }
            });
        }

        // --- Category Dropdown Setup ---
        AutoCompleteTextView categoryDropdown = view.findViewById(R.id.categoryDropdown);
        
        // 1. Load the array from strings.xml
        String[] categories = getResources().getStringArray(R.array.category_array);

        // 2. Create the adapter
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(), 
                android.R.layout.simple_dropdown_item_1line, 
                categories
        );

        // 3. Connect adapter to the view
        categoryDropdown.setAdapter(categoryAdapter);

        // 4. Optional: Show the list as soon as the user clicks the box
        categoryDropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDropdown.showDropDown();
            }
        });
        
        // --- Quantity Units Spinner Setup ---
        Spinner quantitySpinner = view.findViewById(R.id.spinner_units);
        
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.quantity_units, 
                android.R.layout.simple_spinner_item
        );
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        // Set the correct adapter for units
        quantitySpinner.setAdapter(unitAdapter);

        // Date picker
        TextView purchaseDateText = view.findViewById(R.id.purchaseDateText);
        ImageView purchaseCalendarIcon = view.findViewById(R.id.purchaseCalendarIcon);

        purchaseCalendarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                        (view, year1, month1, dayOfMonth) -> {
                            String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                            purchaseDateText.setText(selectedDate);
                        }, year, month, day);
                datePickerDialog.show();
            }
        });
        return view;

    }
}
