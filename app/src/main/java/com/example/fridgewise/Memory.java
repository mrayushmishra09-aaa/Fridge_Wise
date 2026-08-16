package com.example.fridgewise;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Memory#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Memory extends Fragment {

    MaterialCardView cardTodo;

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

        updateCounts(view);

        return view;
    }

    private void updateCounts(View view) {
        TextView tvMedicineCount = view.findViewById(R.id.tvMedicineCount);
        TextView tvTodoCount = view.findViewById(R.id.tvTodoCount);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            int medCount = db.medicineDao().getAllMedicines().size();
            int todoCount = db.todoDao().getAllTodos().size();

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    tvMedicineCount.setText(medCount + (medCount == 1 ? " reminder" : " reminders"));
                    tvTodoCount.setText(todoCount + (todoCount == 1 ? " task" : " tasks"));
                });
            }
        }).start();
    }
}