package com.example.fridgewise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class TodoListFragment extends Fragment {

    private RecyclerView rvTasks;
    private TodoAdapter adapter;
    private List<TodoItem> taskList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo_list, container, false);

        rvTasks = view.findViewById(R.id.rvTasks);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        View btnInfo = view.findViewById(R.id.btnInfo);

        // Dummy Data
        taskList = new ArrayList<>();
        taskList.add(new TodoItem("Submit assignment", "Today, 5:00 PM", "High", false));
        taskList.add(new TodoItem("Buy vegetables", "Today, 7:00 PM", "Medium", false));

        adapter = new TodoAdapter(taskList);
        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTasks.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            // Navigate to Add Todo
            getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView2, new AddTodoFragment()) // Using fragmentContainerView2 from MainActivity
                .addToBackStack(null)
                .commit();
        });

        btnInfo.setOnClickListener(v -> {
            AboutTodoBottomSheet bottomSheet = new AboutTodoBottomSheet();
            bottomSheet.show(getChildFragmentManager(), "AboutTodoBottomSheet");
        });

        return view;
    }
}
