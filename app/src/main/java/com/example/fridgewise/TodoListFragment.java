package com.example.fridgewise;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoListFragment extends Fragment {

    private RecyclerView rvTasks;
    private TodoAdapter adapter;
    private List<TodoItem> allTasks = new ArrayList<>();
    private String currentTab = "Today";
    private TextView tvToday, tvUpcoming, tvCompleted, tvProgressStatus, tvBannerTitle, tvBannerSubtitle;
    private View tabIndicator;
    private LinearProgressIndicator progressIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo_list, container, false);

        rvTasks = view.findViewById(R.id.rvTasks);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        View btnInfo = view.findViewById(R.id.btnInfo);
        ImageButton btnBacktodo = view.findViewById(R.id.btnBacktodo);

        tvToday = view.findViewById(R.id.today);
        tvUpcoming = view.findViewById(R.id.upcoming);
        tvCompleted = view.findViewById(R.id.completed);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        tvProgressStatus = view.findViewById(R.id.tvProgressStatus);
        tvBannerTitle = view.findViewById(R.id.tvBannerTitle);
        tvBannerSubtitle = view.findViewById(R.id.tvBannerSubtitle);
        tabIndicator = view.findViewById(R.id.tabIndicator);

        setupTabs();

        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TodoAdapter(new ArrayList<>());
        adapter.setOnTodoItemClickListener(new TodoAdapter.OnTodoItemClickListener() {
            @Override
            public void onEditClick(TodoItem item) {
                // For now, let's just log or show a toast. 
                // In a real app, you'd navigate to EditFragment.
            }

            @Override
            public void onDeleteClick(TodoItem item) {
                deleteTask(item);
            }

            @Override
            public void onStatusChange(TodoItem item, boolean isCompleted) {
                updateTaskStatus(item);
                updateProgressHeader();
                // Refresh list if we are in Today/Upcoming tab and it was checked
                if (isCompleted && !"Completed".equals(currentTab)) {
                    filterAndDisplayTasks();
                }
            }
        });
        rvTasks.setAdapter(adapter);

        loadTasks();

        fabAdd.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView2, new AddTodoFragment())
                .addToBackStack(null)
                .commit();
        });

        btnInfo.setOnClickListener(v -> {
            AboutTodoBottomSheet bottomSheet = new AboutTodoBottomSheet();
            bottomSheet.show(getChildFragmentManager(), "AboutTodoBottomSheet");
        });

        btnBacktodo.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        return view;
    }

    private void updateProgressHeader() {
        if (allTasks == null || allTasks.isEmpty()) {
            if (progressIndicator != null) progressIndicator.setProgress(0);
            if (tvProgressStatus != null) tvProgressStatus.setText("No tasks yet");
            if (tvBannerTitle != null) tvBannerTitle.setText("Stay Organized");
            if (tvBannerSubtitle != null) tvBannerSubtitle.setText("Add tasks to track your daily progress.");
            return;
        }

        String todayDate = getFormattedTodayDate();
        int totalToday = 0;
        int completedToday = 0;

        for (TodoItem item : allTasks) {
            if (todayDate.equals(item.getDate())) {
                totalToday++;
                if (item.isCompleted()) {
                    completedToday++;
                }
            }
        }

        if (totalToday == 0) {
            if (progressIndicator != null) progressIndicator.setProgress(0);
            if (tvProgressStatus != null) tvProgressStatus.setText("No tasks for today");
            if (tvBannerTitle != null) tvBannerTitle.setText("Daily Goals");
            if (tvBannerSubtitle != null) tvBannerSubtitle.setText("No tasks scheduled for today.");
        } else {
            int percentage = (int) (((float) completedToday / totalToday) * 100);
            if (progressIndicator != null) progressIndicator.setProgress(percentage, true);
            if (tvProgressStatus != null) tvProgressStatus.setText(percentage + "% Completed (" + completedToday + "/" + totalToday + ")");

            // Dynamic messages
            if (tvBannerTitle != null && tvBannerSubtitle != null) {
                if (percentage == 0) {
                    tvBannerTitle.setText("Daily Progress");
                    tvBannerSubtitle.setText("Let's get started on your goals! 🚀");
                } else if (percentage < 50) {
                    tvBannerTitle.setText("Making Progress");
                    tvBannerSubtitle.setText("Nice start! You've got this. 💪");
                } else if (percentage < 100) {
                    tvBannerTitle.setText("Almost There");
                    tvBannerSubtitle.setText("Halfway there! Keep the momentum. 🔥");
                } else {
                    tvBannerTitle.setText("Daily Goal Met");
                    tvBannerSubtitle.setText("You've crushed it! See you tomorrow. 🎉");
                }
            }
        }
    }

    private void setupTabs() {
        // Initial indicator position
        tvToday.post(() -> moveIndicator(tvToday));

        View.OnClickListener tabListener = v -> {
            tvToday.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
            tvUpcoming.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
            tvCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
            tvToday.setTypeface(null, android.graphics.Typeface.NORMAL);
            tvUpcoming.setTypeface(null, android.graphics.Typeface.NORMAL);
            tvCompleted.setTypeface(null, android.graphics.Typeface.NORMAL);

            TextView selectedTab = (TextView) v;
            selectedTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_primary));
            selectedTab.setTypeface(null, android.graphics.Typeface.BOLD);
            
            moveIndicator(selectedTab);

            currentTab = selectedTab.getText().toString();
            filterAndDisplayTasks();
        };

        tvToday.setOnClickListener(tabListener);
        tvUpcoming.setOnClickListener(tabListener);
        tvCompleted.setOnClickListener(tabListener);
    }

    private void moveIndicator(View targetView) {
        if (tabIndicator == null) return;
        
        float targetX = targetView.getX() + (targetView.getWidth() - tabIndicator.getWidth()) / 2f;
        tabIndicator.animate()
                .x(targetX)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void filterAndDisplayTasks() {
        List<TodoItem> filteredList = new ArrayList<>();
        String todayDate = getFormattedTodayDate();

        for (TodoItem item : allTasks) {
            if ("Completed".equalsIgnoreCase(currentTab)) {
                if (item.isCompleted()) filteredList.add(item);
            } else if ("Today".equalsIgnoreCase(currentTab)) {
                if (!item.isCompleted() && todayDate.equals(item.getDate())) filteredList.add(item);
            } else if ("Upcoming".equalsIgnoreCase(currentTab)) {
                if (!item.isCompleted() && isFutureDate(item.getDate())) filteredList.add(item);
            }
        }
        adapter.updateList(filteredList);
    }

    private String getFormattedTodayDate() {
        return new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());
    }

    private boolean isFutureDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            Date taskDate = sdf.parse(dateStr);
            Date today = sdf.parse(getFormattedTodayDate());
            return taskDate != null && taskDate.after(today);
        } catch (Exception e) {
            return false;
        }
    }

    private void loadTasks() {
        Context context = getContext();
        if (context == null) return;
        new Thread(() -> {
            List<TodoItem> tasks = AppDatabase.getInstance(context).todoDao().getAllTodos();
            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(() -> {
                    allTasks.clear();
                    allTasks.addAll(tasks);
                    filterAndDisplayTasks();
                    updateProgressHeader();
                });
            }
        }).start();
    }

    private void deleteTask(TodoItem item) {
        Context context = getContext();
        if (context == null) return;
        new Thread(() -> {
            AppDatabase.getInstance(context).todoDao().delete(item);
            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(() -> {
                    allTasks.remove(item);
                    filterAndDisplayTasks();
                });
            }
        }).start();
    }

    private void updateTaskStatus(TodoItem item) {
        Context context = getContext();
        if (context == null) return;
        new Thread(() -> {
            AppDatabase.getInstance(context).todoDao().update(item);
        }).start();
    }
}
