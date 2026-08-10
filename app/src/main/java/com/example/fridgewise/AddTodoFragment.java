package com.example.fridgewise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AddTodoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_todo, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        Button btnSave = view.findViewById(R.id.btnSave);
        EditText etTitle = view.findViewById(R.id.etTaskTitle);

        btnBack.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        btnSave.setOnClickListener(v -> {
            // Logic to save task would go here
            getActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }
}
