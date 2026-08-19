package com.example.fridgewise;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.transition.TransitionManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        setupDashboard(view);
        setupNavigation(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            setupDashboard(getView());
        }
    }

    private void setupDashboard(View view) {
        LinearLayout llAttentionSection = view.findViewById(R.id.ll_attention_section);
        RecyclerView rvAttention = view.findViewById(R.id.rv_attention);
        TextView tvAttentionCount = view.findViewById(R.id.tv_attention_count);
        
        TextView tvFoodInsight = view.findViewById(R.id.tv_food_insight);
        TextView tvMedInsight = view.findViewById(R.id.tv_medicine_insight);
        TextView tvTodoInsight = view.findViewById(R.id.tv_todo_insight);

        new Thread(() -> {
            Context context = getContext();
            if (context == null) return;
            
            AppDatabase db = AppDatabase.getInstance(context);
            ArrayList<AttentionItem> attentionItems = new ArrayList<>();
            
            Calendar calNow = Calendar.getInstance();
            int currentHour = calNow.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calNow.get(Calendar.MINUTE);
            int currentTotalMinutes = currentHour * 60 + currentMinute;

            // 1. Food Scanning
            List<FoodItem> foodItems = db.foodItemDao().getAllItems();
            String foodInsightMessage = "Your fridge looks good! No items expiring soon.";
            Calendar cal = Calendar.getInstance();
            Date today = resetTime(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, 1);
            Date tomorrow = resetTime(cal.getTime());

            for (FoodItem item : foodItems) {
                try {
                    Date expiry = dateFormat.parse(item.getExpiryDate());
                    if (expiry != null) {
                        expiry = resetTime(expiry);
                        if (expiry.before(today)) {
                            foodInsightMessage = "Alert: " + item.getName() + " has expired! Clean it out.";
                            AttentionItem ai = new AttentionItem(String.valueOf(item.getId()), item.getName(), "Expired", "In Fridge", "Has expired! Clean it out.", "View item", AttentionItem.Type.FOOD);
                            ai.setBadgeBgColor(context.getColor(R.color.badge_red_bg));
                            ai.setBadgeTextColor(context.getColor(R.color.badge_red_text));
                            ai.setStatusColor(context.getColor(R.color.red_expired));
                            ai.setImageResId(R.drawable.vegi_img01);
                            ai.setPriorityScore(10);
                            attentionItems.add(ai);
                        } else if (expiry.equals(today)) {
                            foodInsightMessage = "Today's Insight: " + item.getName() + " expires today. Use it!";
                            AttentionItem ai = new AttentionItem(String.valueOf(item.getId()), item.getName(), "Expires today", "In Fridge", "Use today for best freshness.", "View item", AttentionItem.Type.FOOD);
                            ai.setBadgeBgColor(context.getColor(R.color.badge_red_bg));
                            ai.setBadgeTextColor(context.getColor(R.color.badge_red_text));
                            ai.setStatusColor(context.getColor(R.color.red_expired));
                            ai.setImageResId(R.drawable.vegi_img01);
                            ai.setPriorityScore(50);
                            attentionItems.add(ai);
                        } else if (expiry.equals(tomorrow)) {
                            if (foodInsightMessage.startsWith("Your fridge")) foodInsightMessage = "Insight: " + item.getName() + " will expire tomorrow.";
                            AttentionItem ai = new AttentionItem(String.valueOf(item.getId()), item.getName(), "Expires tomorrow", "In Fridge", "Expires tomorrow. Plan ahead.", "View item", AttentionItem.Type.FOOD);
                            ai.setBadgeBgColor(context.getColor(R.color.badge_orange_bg));
                            ai.setBadgeTextColor(context.getColor(R.color.badge_orange_text));
                            ai.setStatusColor(context.getColor(R.color.orange_warning));
                            ai.setImageResId(R.drawable.vegi_img01);
                            ai.setPriorityScore(90);
                            attentionItems.add(ai);
                        }
                    }
                } catch (ParseException e) { /* Ignore invalid dates */ }
            }

            // 2. Medicine Scanning
            List<MedicineEntity> medicines = db.medicineDao().getAllMedicines();
            String medInsightMessage = "No medicines scheduled for today.";
            int medCount = 0;
            for (MedicineEntity med : medicines) {
                if (med.isReminderOn()) {
                    medCount++;
                    String time = med.getStartTime(); // Assuming "HH:mm"
                    int medMinutes = parseMinutes(time);
                    long score = 120; // Default
                    String hint = "Scheduled for today.";
                    
                    if (medMinutes >= currentTotalMinutes) {
                        int diff = medMinutes - currentTotalMinutes;
                        if (diff <= 120) { // Due within 2 hours
                            score = 30 + diff;
                            hint = "Take in " + (diff / 60 > 0 ? (diff / 60) + "h " : "") + (diff % 60) + "m.";
                        } else {
                            score = 120 + diff;
                        }
                    } else {
                        // Past due for today but reminder still on? 
                        score = 200; 
                        hint = "Missed or past scheduled time.";
                    }

                    AttentionItem ai = new AttentionItem(String.valueOf(med.getId()), med.getMedicineName(), time, "Medicine reminder", hint, "View reminder", AttentionItem.Type.MEDICINE);
                    ai.setBadgeBgColor(context.getColor(R.color.badge_purple_bg));
                    ai.setBadgeTextColor(context.getColor(R.color.badge_purple_text));
                    ai.setStatusColor(context.getColor(R.color.purple_primary));
                    ai.setImageResId(R.drawable.med_image_07);
                    ai.setPriorityScore(score);
                    attentionItems.add(ai);
                }
            }
            if (medCount > 0) medInsightMessage = "Reminder: You have " + medCount + " active medicine schedules.";

            // 3. To-Do Scanning
            List<TodoItem> pendingTodos = db.todoDao().getPendingTodos();
            String todoInsightMessage = "No pending tasks for today.";
            for (TodoItem todo : pendingTodos) {
                String priority = todo.getPriority() != null ? todo.getPriority().toLowerCase() : "medium";
                long score = 150;
                int dotColor = R.color.green_primary;
                int bgColor = R.color.badge_purple_bg; // Reusing purple for tasks or use another
                int txtColor = R.color.badge_purple_text;

                if (priority.contains("high")) {
                    score = 70;
                    dotColor = R.color.red_expired;
                } else if (priority.contains("medium")) {
                    score = 150;
                    dotColor = R.color.orange_warning;
                }

                AttentionItem ai = new AttentionItem(String.valueOf(todo.getId()), todo.getTitle(), todo.getPriority(), "To-Do List", "High priority task pending.", "View task", AttentionItem.Type.TODO);
                ai.setBadgeBgColor(context.getColor(bgColor));
                ai.setBadgeTextColor(context.getColor(txtColor));
                ai.setStatusColor(context.getColor(dotColor));
                ai.setImageResId(R.drawable.ic_todo_item);
                ai.setPriorityScore(score);
                if (priority.contains("high")) attentionItems.add(ai); // Only show high priority in attention
            }
            if (!pendingTodos.isEmpty()) todoInsightMessage = "Task: You have " + pendingTodos.size() + " items on your to-do list.";

            // 4. Sorting & Finalizing
            attentionItems.sort(Comparator.comparingLong(AttentionItem::getPriorityScore));

            final String fFoodMsg = foodInsightMessage;
            final String fMedMsg = medInsightMessage;
            final String fTodoMsg = todoInsightMessage;
            final int attentionSize = attentionItems.size();

            Activity activity = getActivity();
            if (activity != null && isAdded()) {
                activity.runOnUiThread(() -> {
                    if (!isAdded()) return;

                    ImageView ivAttentionIcon = view.findViewById(R.id.iv_attention_icon);
                    TextView tvAttentionTitle = view.findViewById(R.id.tv_attention_title);
                    View cvAttentionEmpty = view.findViewById(R.id.cv_attention_empty);

                    if (attentionSize > 0) {
                        if (llAttentionSection != null) llAttentionSection.setVisibility(View.VISIBLE);
                        if (cvAttentionEmpty != null) cvAttentionEmpty.setVisibility(View.GONE);
                        if (rvAttention != null) rvAttention.setVisibility(View.VISIBLE);
                        if (ivAttentionIcon != null) {
                            ivAttentionIcon.setImageResource(R.drawable.ic_attention_red);
                            ivAttentionIcon.clearColorFilter();
                        }
                        if (tvAttentionTitle != null) tvAttentionTitle.setText("Needs your attention");
                        if (tvAttentionCount != null) {
                            tvAttentionCount.setText(attentionSize + " things need your attention today");
                        }
                        if (rvAttention != null) {
                            AttentionAdapter adapter = new AttentionAdapter(context, attentionItems, item -> {
                                Fragment targetFragment = null;
                                if (item.getType() == AttentionItem.Type.FOOD) {
                                    targetFragment = new InventoryFragment();
                                } else if (item.getType() == AttentionItem.Type.MEDICINE) {
                                    targetFragment = new Med_section();
                                } else if (item.getType() == AttentionItem.Type.TODO) {
                                    targetFragment = new TodoListFragment();
                                }

                                if (targetFragment != null) {
                                    getParentFragmentManager().beginTransaction()
                                            .replace(R.id.fragmentContainerView2, targetFragment)
                                            .addToBackStack(null)
                                            .commit();
                                }
                            });
                            rvAttention.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                            rvAttention.setAdapter(adapter);
                        }
                    } else {
                        if (llAttentionSection != null) llAttentionSection.setVisibility(View.VISIBLE);
                        if (cvAttentionEmpty != null) cvAttentionEmpty.setVisibility(View.VISIBLE);
                        if (rvAttention != null) rvAttention.setVisibility(View.GONE);
                        if (ivAttentionIcon != null) {
                            ivAttentionIcon.setImageResource(R.drawable.ic_check_tick);
                            ivAttentionIcon.setColorFilter(context.getColor(R.color.green_primary));
                        }
                        if (tvAttentionTitle != null) tvAttentionTitle.setText("Everything is in order");
                        if (tvAttentionCount != null) {
                            tvAttentionCount.setText("You're all caught up for now!");
                        }
                    }
                    
                    if (tvFoodInsight != null) tvFoodInsight.setText(fFoodMsg);
                    if (tvMedInsight != null) tvMedInsight.setText(fMedMsg);
                    if (tvTodoInsight != null) tvTodoInsight.setText(fTodoMsg);
                });
            }
        }).start();
    }

    private int parseMinutes(String time) {
        if (time == null || !time.contains(":")) return 0;
        try {
            String[] parts = time.split(":");
            int h = Integer.parseInt(parts[0].trim());
            int m = Integer.parseInt(parts[1].trim().split(" ")[0]); // Handle AM/PM if present
            if (time.toLowerCase().contains("pm") && h < 12) h += 12;
            if (time.toLowerCase().contains("am") && h == 12) h = 0;
            return h * 60 + m;
        } catch (Exception e) { return 0; }
    }

    private Date resetTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private void setupNavigation(View view) {
        // Quick Add Section Toggle
        LinearLayout llQuickAddHeader = view.findViewById(R.id.ll_quick_add_header);
        LinearLayout llQuickAddOptions = view.findViewById(R.id.ll_quick_add_options);
        ImageView ivExpandArrow = view.findViewById(R.id.iv_expand_arrow);

        if (llQuickAddHeader != null && llQuickAddOptions != null) {
            llQuickAddHeader.setOnClickListener(v -> {
                boolean isVisible = llQuickAddOptions.getVisibility() == View.VISIBLE;
                TransitionManager.beginDelayedTransition((ViewGroup) view.findViewById(R.id.cv_quick_add));
                llQuickAddOptions.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                if (ivExpandArrow != null) {
                    ivExpandArrow.animate().rotation(isVisible ? 0 : 90).start();
                }
            });
        }

        // Quick Add Buttons
        View btnAddFood = view.findViewById(R.id.btn_add_food);
        if (btnAddFood != null) {
            btnAddFood.setOnClickListener(v -> replaceFragment(new AddItemFragment()));
        }

        View btnAddMedicine = view.findViewById(R.id.btn_add_medicine);
        if (btnAddMedicine != null) {
            btnAddMedicine.setOnClickListener(v -> replaceFragment(new MedicineAddFragment()));
        }

        View btnAddTodo = view.findViewById(R.id.btn_add_todo);
        if (btnAddTodo != null) {
            btnAddTodo.setOnClickListener(v -> replaceFragment(new AddTodoFragment()));
        }

        View btnAddShopping = view.findViewById(R.id.btn_add_shopping);
        if (btnAddShopping != null) {
            btnAddShopping.setOnClickListener(v -> replaceFragment(new ShoppingListFragment()));
        }

        // Search Bar Logic (placeholder)
        View searchBar = view.findViewById(R.id.search_bar);
        if (searchBar != null) {
            searchBar.setOnClickListener(v -> {
                // Future search implementation
            });
        }

        // Attention Section Logic
        if (view.findViewById(R.id.tv_view_all_attention) != null) {
            view.findViewById(R.id.tv_view_all_attention).setOnClickListener(v -> {
                replaceFragment(new AlertsFragment());
            });
        }
    }

    private void replaceFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView2, fragment)
                .addToBackStack(null)
                .commit();
    }
}