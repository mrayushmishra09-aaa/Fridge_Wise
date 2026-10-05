package com.example.fridgewise.ui.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fridgewise.R;
import com.example.fridgewise.data.AppDatabase;
import com.example.fridgewise.data.PreferenceManager;
import com.example.fridgewise.model.CustomSpace;
import com.example.fridgewise.model.TodoItem;
import com.example.fridgewise.ui.bottomsheet.StreakPreferencesBottomSheet;
import com.example.fridgewise.util.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StreakFragment extends Fragment {

    private ProgressBar pbStreakToday;
    private TextView tvProgressPercent;
    private TextView tvTaskCountRatio;
    private TextView tvStatCompleted;
    private TextView tvStatRemaining;
    private TextView tvStreakDays;
    private RecyclerView rvStreakTasks;

    private StreakTaskAdapter taskAdapter;
    private PreferenceManager prefManager;

    private final View[] heatmapCells = new View[28];

    public StreakFragment() {
        // Required empty public constructor
    }

    public static StreakFragment newInstance() {
        return new StreakFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_streak, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefManager = new PreferenceManager(requireContext());

        // Bind Views
        ImageView btnBack = view.findViewById(R.id.btnBack);
        ImageView btnSettings = view.findViewById(R.id.btnSettings);
        TextView btnManageTasks = view.findViewById(R.id.btnManageTasks);

        tvStreakDays = view.findViewById(R.id.tvStreakDays);
        pbStreakToday = view.findViewById(R.id.pbStreakToday);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        tvTaskCountRatio = view.findViewById(R.id.tvTaskCountRatio);
        tvStatCompleted = view.findViewById(R.id.tvStatCompleted);
        tvStatRemaining = view.findViewById(R.id.tvStatRemaining);

        rvStreakTasks = view.findViewById(R.id.rvStreakTasks);

        // Bind Heatmap Cells
        bindHeatmapCells(view);

        // Update Dynamic Streak Days Banner
        updateStreakBanner();

        // Back Listener
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        // Settings Listener
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> openStreakPreferences());
        }

        // Manage Tasks Listener
        if (btnManageTasks != null) {
            btnManageTasks.setOnClickListener(v -> 
                Navigation.findNavController(v).navigate(R.id.todoListFragment)
            );
        }

        // Setup Category Filter Chips
        setupDynamicChips(view);

        // Setup Month Filter Popup
        TextView tvMonthFilter = view.findViewById(R.id.tvMonthFilter);
        if (tvMonthFilter != null) {
            tvMonthFilter.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(requireContext(), v);
                popup.getMenu().add("This Month");
                popup.getMenu().add("Last 30 Days");
                popup.getMenu().add("Last 3 Months");
                popup.setOnMenuItemClickListener(item -> {
                    tvMonthFilter.setText(item.getTitle() + " ⌄");
                    Toast.makeText(requireContext(), "Viewing: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    updateHeatmapDisplay();
                    return true;
                });
                popup.show();
            });
        }

        // Setup Tasks
        setupTasks();

        // Setup Dynamic Heatmap
        updateHeatmapDisplay();
    }

    private void updateStreakBanner() {
        if (prefManager != null && tvStreakDays != null) {
            int streakDays = prefManager.getStreakCount();
            tvStreakDays.setText(String.format(Locale.getDefault(), "%d Days", streakDays));
        }
    }

    private void openStreakPreferences() {
        StreakPreferencesBottomSheet sheet = new StreakPreferencesBottomSheet();
        sheet.setOnPreferencesChangedListener(() -> {
            calculateAndSyncStreak();
            updateHeatmapDisplay();
            updateStreakBanner();
            NotificationHelper.updatePinnedStreakNotification(requireContext());
        });
        sheet.show(getChildFragmentManager(), "StreakPreferences");
    }

    private final List<CustomSpace> latestCustomSpaces = new ArrayList<>();
    private final List<TodoItem> latestTodoItems = new ArrayList<>();
    private String currentSelectedTag = "All";

    private void setupDynamicChips(View rootView) {
        LinearLayout container = rootView.findViewById(R.id.llCategoryChipsContainer);
        if (container == null) return;

        AppDatabase db = AppDatabase.getInstance(requireContext());

        db.customSpaceDao().getAllSpaces().observe(getViewLifecycleOwner(), spaces -> {
            latestCustomSpaces.clear();
            if (spaces != null) {
                latestCustomSpaces.addAll(spaces);
            }
            rebuildChips(container);
        });

        db.todoDao().getAllTodosLiveData().observe(getViewLifecycleOwner(), todos -> {
            latestTodoItems.clear();
            if (todos != null) {
                latestTodoItems.addAll(todos);
            }
            rebuildChips(container);
        });
    }

    private void rebuildChips(LinearLayout container) {
        if (container == null) return;
        container.removeAllViews();

        List<String> connectedSpaceNames = new ArrayList<>();

        // 1. Add CustomSpaces connected to Progress (where isStreakEnabled is true)
        for (CustomSpace s : latestCustomSpaces) {
            if (s != null && s.isStreakEnabled() && s.getName() != null && !s.getName().trim().isEmpty()) {
                String name = s.getName().trim();
                if (!containsIgnoreCase(connectedSpaceNames, name)) {
                    connectedSpaceNames.add(name);
                }
            }
        }

        // 2. Add To-Do space names connected to Progress (where countTowardsStreak is true)
        for (TodoItem t : latestTodoItems) {
            if (t != null && t.isCountTowardsStreak() && t.getSpaceName() != null && !t.getSpaceName().trim().isEmpty()) {
                String name = t.getSpaceName().trim();
                if (!"General".equalsIgnoreCase(name) && !containsIgnoreCase(connectedSpaceNames, name)) {
                    connectedSpaceNames.add(name);
                }
            }
        }

        // Validate selection
        boolean isSelectedTagValid = "All".equalsIgnoreCase(currentSelectedTag);
        if (!isSelectedTagValid) {
            for (String name : connectedSpaceNames) {
                if (name.equalsIgnoreCase(currentSelectedTag)) {
                    isSelectedTagValid = true;
                    break;
                }
            }
        }
        if (!isSelectedTagValid) {
            currentSelectedTag = "All";
        }

        // 1. Add [All] Chip
        TextView chipAll = createChipTextView("All", "All".equalsIgnoreCase(currentSelectedTag));
        chipAll.setOnClickListener(v -> {
            currentSelectedTag = "All";
            highlightSelectedChip(container, chipAll);
            filterTasksByTag("All");
        });
        container.addView(chipAll);

        // 2. Add Connected Space Chips
        for (String spaceName : connectedSpaceNames) {
            boolean isSelected = spaceName.equalsIgnoreCase(currentSelectedTag);
            String label = spaceName + " 🔥";
            TextView spaceChip = createChipTextView(label, isSelected);
            spaceChip.setOnClickListener(v -> {
                currentSelectedTag = spaceName;
                highlightSelectedChip(container, spaceChip);
                filterTasksByTag(spaceName);
            });
            container.addView(spaceChip);
        }

        // 3. Add [+] Chip
        TextView chipAdd = createChipTextView("＋", false);
        chipAdd.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.createSpaceFragment)
        );
        container.addView(chipAdd);
    }

    private boolean containsIgnoreCase(List<String> list, String val) {
        for (String item : list) {
            if (item.equalsIgnoreCase(val)) return true;
        }
        return false;
    }

    private TextView createChipTextView(String text, boolean isSelected) {
        TextView tv = new TextView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        float density = getResources().getDisplayMetrics().density;
        params.setMarginEnd((int) (8 * density));
        tv.setLayoutParams(params);

        tv.setText(text);
        tv.setTextSize(13);
        tv.setPadding((int) (18 * density), (int) (8 * density), (int) (18 * density), (int) (8 * density));

        if (isSelected) {
            tv.setBackgroundResource(R.drawable.bg_chip_light_purple);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.streak_chip_selected_text));
            tv.setTypeface(null, Typeface.BOLD);
        } else {
            tv.setBackgroundResource(R.drawable.bg_stat_pill);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.streak_text_primary));
            tv.setTypeface(null, Typeface.NORMAL);
        }
        return tv;
    }

    private void highlightSelectedChip(LinearLayout container, TextView selectedTv) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                if (tv == selectedTv) {
                    tv.setBackgroundResource(R.drawable.bg_chip_light_purple);
                    tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.streak_chip_selected_text));
                    tv.setTypeface(null, Typeface.BOLD);
                } else {
                    tv.setBackgroundResource(R.drawable.bg_stat_pill);
                    tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.streak_text_primary));
                    tv.setTypeface(null, Typeface.NORMAL);
                }
            }
        }
    }

    private final List<TodoItem> roomTodoList = new ArrayList<>();

    private void filterTasksByTag(String tag) {
        currentSelectedTag = tag;
        filterAndDisplayTasks();
    }

    private void setupTasks() {
        taskAdapter = new StreakTaskAdapter(new ArrayList<>(), task -> {
            task.setCompleted(!task.isCompleted());
            new Thread(() -> {
                AppDatabase.getInstance(requireContext()).todoDao().update(task);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        calculateAndSyncStreak();
                        updateHeatmapDisplay();
                    });
                }
                NotificationHelper.updatePinnedStreakNotification(requireContext());
            }).start();
        });

        rvStreakTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvStreakTasks.setAdapter(taskAdapter);

        // Observe Room LiveData for Todos
        AppDatabase db = AppDatabase.getInstance(requireContext());
        db.todoDao().getAllTodosLiveData().observe(getViewLifecycleOwner(), todos -> {
            roomTodoList.clear();
            if (todos != null) {
                for (TodoItem item : todos) {
                    if (item.isCountTowardsStreak()) {
                        roomTodoList.add(item);
                    }
                }
            }
            filterAndDisplayTasks();
            calculateAndSyncStreak();
            updateHeatmapDisplay();
        });
    }

    private void filterAndDisplayTasks() {
        List<TodoItem> filtered = new ArrayList<>();
        for (TodoItem task : roomTodoList) {
            if ("All".equalsIgnoreCase(currentSelectedTag) || task.getSpaceName().equalsIgnoreCase(currentSelectedTag)) {
                filtered.add(task);
            }
        }
        taskAdapter.setTasks(filtered);
        updateProgressMetrics(filtered);
    }

    private void updateProgressMetrics(List<TodoItem> currentTasks) {
        int total = currentTasks.size();
        int completed = 0;
        for (TodoItem t : currentTasks) {
            if (t.isCompleted()) completed++;
        }

        int percent = total > 0 ? (completed * 100) / total : 0;

        if (pbStreakToday != null) pbStreakToday.setProgress(percent);
        if (tvProgressPercent != null) tvProgressPercent.setText(String.format(Locale.getDefault(), "%d%%", percent));
        if (tvTaskCountRatio != null) tvTaskCountRatio.setText(String.format(Locale.getDefault(), "%d / %d tasks", completed, total));
        if (tvStatCompleted != null) tvStatCompleted.setText(String.format(Locale.getDefault(), "✓  %d completed", completed));
        if (tvStatRemaining != null) tvStatRemaining.setText(String.format(Locale.getDefault(), "🕒  %d remaining", (total - completed)));
    }

    private void bindHeatmapCells(View v) {
        int[] cellIds = new int[]{
                R.id.cell_w1_1, R.id.cell_w1_2, R.id.cell_w1_3, R.id.cell_w1_4, R.id.cell_w1_5, R.id.cell_w1_6, R.id.cell_w1_7,
                R.id.cell_w2_1, R.id.cell_w2_2, R.id.cell_w2_3, R.id.cell_w2_4, R.id.cell_w2_5, R.id.cell_w2_6, R.id.cell_w2_7,
                R.id.cell_w3_1, R.id.cell_w3_2, R.id.cell_w3_3, R.id.cell_w3_4, R.id.cell_w3_5, R.id.cell_w3_6, R.id.cell_w3_7,
                R.id.cell_w4_1, R.id.cell_w4_2, R.id.cell_w4_3, R.id.cell_w4_4, R.id.cell_w4_5, R.id.cell_w4_6, R.id.cell_w4_7
        };
        for (int i = 0; i < 28; i++) {
            heatmapCells[i] = v.findViewById(cellIds[i]);
        }
    }

    private void updateHeatmapDisplay() {
        if (latestTodoItems == null) return;

        SimpleDateFormat sdfDisplay = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        for (int i = 0; i < 28; i++) {
            if (heatmapCells[i] == null) continue;

            int daysAgo = 27 - i;
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo);

            String displayDate = sdfDisplay.format(cal.getTime());
            int completedCount = countCompletedTasksForDate(cal);

            int drawableId;
            if (completedCount >= 5) {
                drawableId = R.drawable.bg_heatmap_cell_4;
            } else if (completedCount >= 3) {
                drawableId = R.drawable.bg_heatmap_cell_3;
            } else if (completedCount >= 2) {
                drawableId = R.drawable.bg_heatmap_cell_2;
            } else if (completedCount == 1) {
                drawableId = R.drawable.bg_heatmap_cell_1;
            } else {
                drawableId = R.drawable.bg_heatmap_cell_0;
            }

            heatmapCells[i].setBackgroundResource(drawableId);

            final int count = completedCount;
            heatmapCells[i].setOnClickListener(v -> {
                String taskText = count == 1 ? "1 completed task" : count + " completed tasks";
                Toast.makeText(requireContext(), displayDate + ": " + taskText, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private int countCompletedTasksForDate(Calendar cal) {
        if (latestTodoItems == null) return 0;

        int count = 0;
        int targetYear = cal.get(Calendar.YEAR);
        int targetDay = cal.get(Calendar.DAY_OF_YEAR);

        SimpleDateFormat sdfStandard = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        String targetDateStr = sdfStandard.format(cal.getTime());

        for (TodoItem item : latestTodoItems) {
            if (item == null || !item.isCompleted() || !item.isCountTowardsStreak()) {
                continue;
            }

            boolean isMatch = false;
            if (item.getStatusChangeTime() > 0) {
                Calendar taskCal = Calendar.getInstance();
                taskCal.setTimeInMillis(item.getStatusChangeTime());
                if (taskCal.get(Calendar.YEAR) == targetYear && taskCal.get(Calendar.DAY_OF_YEAR) == targetDay) {
                    isMatch = true;
                }
            }

            if (!isMatch && item.getDate() != null && !item.getDate().trim().isEmpty()) {
                if (targetDateStr.equalsIgnoreCase(item.getDate().trim())) {
                    isMatch = true;
                } else {
                    try {
                        Date taskDate = sdfStandard.parse(item.getDate().trim());
                        if (taskDate != null) {
                            Calendar taskCal = Calendar.getInstance();
                            taskCal.setTime(taskDate);
                            if (taskCal.get(Calendar.YEAR) == targetYear && taskCal.get(Calendar.DAY_OF_YEAR) == targetDay) {
                                isMatch = true;
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            if (isMatch) {
                count++;
            }
        }
        return count;
    }

    private void calculateAndSyncStreak() {
        if (prefManager == null) return;

        int dailyTarget = prefManager.getDailyTaskTarget();
        if (dailyTarget <= 0) dailyTarget = 1;

        // Check Today (0 days ago)
        Calendar todayCal = Calendar.getInstance();
        int todayCompleted = countCompletedTasksForDate(todayCal);
        boolean isTodayGoalMet = (todayCompleted >= dailyTarget);

        // Check Yesterday (1 day ago)
        Calendar yesterdayCal = Calendar.getInstance();
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1);
        int yesterdayCompleted = countCompletedTasksForDate(yesterdayCal);
        boolean isYesterdayGoalMet = (yesterdayCompleted >= dailyTarget);

        int activeStreak = 0;

        if (isTodayGoalMet) {
            activeStreak = 1;
            int daysAgo = 1;
            while (daysAgo < 365) {
                Calendar checkCal = Calendar.getInstance();
                checkCal.add(Calendar.DAY_OF_YEAR, -daysAgo);
                if (countCompletedTasksForDate(checkCal) >= dailyTarget) {
                    activeStreak++;
                    daysAgo++;
                } else {
                    break;
                }
            }
        } else if (isYesterdayGoalMet) {
            activeStreak = 1;
            int daysAgo = 2;
            while (daysAgo < 365) {
                Calendar checkCal = Calendar.getInstance();
                checkCal.add(Calendar.DAY_OF_YEAR, -daysAgo);
                if (countCompletedTasksForDate(checkCal) >= dailyTarget) {
                    activeStreak++;
                    daysAgo++;
                } else {
                    break;
                }
            }
        } else {
            activeStreak = 0; // Streak broken!
        }

        prefManager.setStreakCount(activeStreak);
        String todayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        prefManager.setLastStreakDate(todayKey);

        updateStreakBanner();
        NotificationHelper.updatePinnedStreakNotification(requireContext());
    }

    // --- Inner Data Models & Adapters ---

    private static class StreakTaskAdapter extends RecyclerView.Adapter<StreakTaskAdapter.TaskViewHolder> {
        private List<TodoItem> tasks;
        private final OnTaskClickListener listener;

        interface OnTaskClickListener {
            void onTaskClick(TodoItem task);
        }

        public StreakTaskAdapter(List<TodoItem> tasks, OnTaskClickListener listener) {
            this.tasks = tasks;
            this.listener = listener;
        }

        public void setTasks(List<TodoItem> newTasks) {
            this.tasks = newTasks;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_streak_task, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            TodoItem task = tasks.get(position);
            holder.tvTitle.setText(task.getTitle());

            String timeText = task.getTime() != null && !task.getTime().isEmpty() ? task.getTime() : "Today";
            holder.tvSubtitle.setText(timeText + "  •  " + task.getSpaceName());
            holder.tvPriority.setText(task.getPriority() != null ? task.getPriority() : "Low");

            // Checkbox state
            if (task.isCompleted()) {
                holder.ivCheck.setImageResource(R.drawable.bg_checkbox_selected);
            } else {
                holder.ivCheck.setImageResource(R.drawable.bg_checkbox_unselected);
            }

            // Priority Chip Style
            if ("High".equalsIgnoreCase(task.getPriority())) {
                holder.tvPriority.setBackgroundResource(R.drawable.bg_priority_high_chip);
                holder.tvPriority.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.streak_prio_high_text));
            } else if ("Medium".equalsIgnoreCase(task.getPriority())) {
                holder.tvPriority.setBackgroundResource(R.drawable.bg_priority_med_chip);
                holder.tvPriority.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.streak_prio_med_text));
            } else {
                holder.tvPriority.setBackgroundResource(R.drawable.bg_priority_low_chip);
                holder.tvPriority.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.streak_prio_low_text));
            }

            holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
            holder.ivCheck.setOnClickListener(v -> listener.onTaskClick(task));
        }

        @Override
        public int getItemCount() { return tasks.size(); }

        static class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSubtitle, tvPriority;
            ImageView ivCheck;

            TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTaskTitle);
                tvSubtitle = itemView.findViewById(R.id.tvTaskSubtitle);
                tvPriority = itemView.findViewById(R.id.tvTaskPriority);
                ivCheck = itemView.findViewById(R.id.ivTaskCheck);
            }
        }
    }
}
