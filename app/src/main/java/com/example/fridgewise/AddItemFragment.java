package com.example.fridgewise;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class AddItemFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    // 👇 THIS IS YOUR BACK ARROW VARIABLE
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
        // Connect to your XML layout
        View view = inflater.inflate(R.layout.fragment_add_item, container, false);

        // 👇 SET UP YOUR BACK ARROW BUTTON
        img_01 = view.findViewById(R.id.back_arrow);
        img_01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This goes back to the previous screen
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        // If you have other code (like save button, etc.), add it HERE
        // Example:
        // Button saveButton = view.findViewById(R.id.saveButton);
        // saveButton.setOnClickListener(...);

        return view;
    }
}