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
import android.widget.TextView;
import android.transition.TransitionManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        
        View cvTodayInsight = view.findViewById(R.id.cv_today_insight);
        TextView tvInsightTitle = view.findViewById(R.id.tv_insight_title);
        TextView tvInsightDesc = view.findViewById(R.id.tv_insight_description);
        View btnInsightAction = view.findViewById(R.id.btn_insight_action);

        new Thread(() -> {
            Context context = getContext();
            if (context == null) return;
            
            AppDatabase db = AppDatabase.getInstance(context);
            ArrayList<AttentionItem> attentionItems = new ArrayList<>();
            
            String insightTitle = null;
            String insightDesc = null;
            Fragment insightTargetFragment = null;

            Calendar calNow = Calendar.getInstance();
            int currentTotalMinutes = calNow.get(Calendar.HOUR_OF_DAY) * 60 + calNow.get(Calendar.MINUTE);

            // 1. Food Data
            List<FoodItem> foodItems = db.foodItemDao().getAllItems();
            Date today = resetTime(calNow.getTime());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 1);
            Date tomorrow = resetTime(cal.getTime());

            for (FoodItem item : foodItems) {
                if (insightTitle == null && item.getQuantity() <= 1) {
                    insightTitle = item.getName() + " is running low";
                    insightDesc = "You have very little " + item.getName() + " left. Add to shopping list?";
                    insightTargetFragment = new ShoppingListFragment();
                }

                try {
                    Date expiry = dateFormat.parse(item.getExpiryDate());
                    if (expiry != null) {
                        expiry = resetTime(expiry);
                        if (expiry.before(today)) {
                            AttentionItem ai = new AttentionItem(String.valueOf(item.getId()), item.getName(), "Expired", "In Fridge", "Has expired! Clean it out.", "View item", AttentionItem.Type.FOOD);
                            ai.setPriorityScore(10);
                            ai.setImageResId(CategoryUtils.getCategoryIcon(item.getCategory()));
                            attentionItems.add(ai);
                        } else if (expiry.equals(today)) {
                            AttentionItem ai = new AttentionItem(String.valueOf(item.getId()), item.getName(), "Expires today", "In Fridge", "Use today for best freshness.", "View item", AttentionItem.Type.FOOD);
                            ai.setPriorityScore(50);
                            ai.setImageResId(CategoryUtils.getCategoryIcon(item.getCategory()));
                            attentionItems.add(ai);
                        }
                    }
                } catch (ParseException e) { }
            }

            // 2. Meds
            List<MedicineEntity> medicines = db.medicineDao().getAllMedicines();
            int activeMedCount = 0;
            for (MedicineEntity med : medicines) {
                if (med.isReminderOn()) {
                    activeMedCount++;
                    AttentionItem ai = new AttentionItem(String.valueOf(med.getId()), med.getMedicineName(), med.getStartTime(), "Medicine", "Time to take your meds.", "View", AttentionItem.Type.MEDICINE);
                    ai.setPriorityScore(100);
                    ai.setImageResId(med.getIconResId());
                    attentionItems.add(ai);
                }
            }

            // 3. Tasks
            List<TodoItem> pendingTodos = db.todoDao().getPendingTodos();
            for (TodoItem todo : pendingTodos) {
                if ("High".equalsIgnoreCase(todo.getPriority())) {
                    AttentionItem ai = new AttentionItem(String.valueOf(todo.getId()), todo.getTitle(), "High Priority", "Tasks", "Pending task needs completion.", "View", AttentionItem.Type.TODO);
                    ai.setPriorityScore(70);
                    ai.setImageResId(R.drawable.ic_todo_item);
                    attentionItems.add(ai);
                }
            }

            attentionItems.sort((a, b) -> Long.compare(b.getPriorityScore(), a.getPriorityScore()));

            // Time for Greeting
            final String currentTime;
            int hour = calNow.get(Calendar.HOUR_OF_DAY);
            if (hour >= 5 && hour < 12) currentTime = "Morning";
            else if (hour >= 12 && hour < 17) currentTime = "Afternoon";
            else if (hour >= 17 && hour < 21) currentTime = "Evening";
            else currentTime = "Night";

            final String fInsightTitle = insightTitle;
            final String fInsightDesc = insightDesc;
            final Fragment fTargetFragment = insightTargetFragment;
            final int fMedCount = activeMedCount;
            final int fTodoCount = pendingTodos.size();
            final int attentionSize = attentionItems.size();

            Activity activity = getActivity();
            if (activity != null && isAdded()) {
                activity.runOnUiThread(() -> {
                    if (!isAdded()) return;

                    // Set Greeting
                    TextView tvUserMessage = view.findViewById(R.id.tv_user_message);
                    if (tvUserMessage != null) tvUserMessage.setText("Good " + currentTime + "! Ready for a healthy meal?");

                    // Attention Section
                    if (attentionSize > 0) {
                        llAttentionSection.setVisibility(View.VISIBLE);
                        rvAttention.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.cv_attention_empty).setVisibility(View.GONE);
                        tvAttentionCount.setText(attentionSize + " things need attention");
                        rvAttention.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                        rvAttention.setAdapter(new AttentionAdapter(context, attentionItems, item -> {
                             // Navigation logic...
                        }));
                    } else {
                        view.findViewById(R.id.cv_attention_empty).setVisibility(View.VISIBLE);
                        rvAttention.setVisibility(View.GONE);
                        tvAttentionCount.setText("Everything is in order");
                    }

                    // Smart AI Insight
                    if (cvTodayInsight != null) {
                        cvTodayInsight.setVisibility(View.VISIBLE);
                        tvInsightTitle.setText("Thinking...");
                        
                        StringBuilder data = new StringBuilder("Time: " + currentTime + ". ");
                        data.append("Food count: ").append(foodItems.size()).append(". ");
                        data.append("Meds: ").append(fMedCount).append(". ");
                        data.append("Tasks: ").append(fTodoCount).append(".");

                        // Use API key from BuildConfig
                        String apiKey = BuildConfig.GEMINI_API_KEY;
                        new GeminiManager(apiKey).getSmartInsight(data.toString(), new GeminiManager.InsightCallback() {
                            @Override
                            public void onInsightGenerated(String greeting, String title, String description) {
                                activity.runOnUiThread(() -> {
                                    // 1. Update Top Greeting
                                    if (tvUserMessage != null) tvUserMessage.setText(greeting);
                                    
                                    // 2. Update Insight Card
                                    tvInsightTitle.setText(title);
                                    tvInsightDesc.setText(description);
                                    
                                    if (btnInsightAction != null && fTargetFragment != null) {
                                        btnInsightAction.setOnClickListener(v -> replaceFragment(fTargetFragment));
                                    }
                                });
                            }

                            @Override
                            public void onError(Throwable t) {
                                activity.runOnUiThread(() -> {
                                    // Fallback to manual insight or a welcome message
                                    if (fInsightTitle != null) {
                                        tvInsightTitle.setText(fInsightTitle);
                                        tvInsightDesc.setText(fInsightDesc);
                                    } else {
                                        tvInsightTitle.setText("Welcome to FridgeWise!");
                                        tvInsightDesc.setText("Add your first item to start getting smart insights.");
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private int parseMinutes(String time) {
        try {
            String[] parts = time.split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1].split(" ")[0]);
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
        View btnAddFood = view.findViewById(R.id.btn_add_food);
        if (btnAddFood != null) btnAddFood.setOnClickListener(v -> replaceFragment(new AddItemFragment()));
        
        View btnAddMed = view.findViewById(R.id.btn_add_medicine);
        if (btnAddMed != null) btnAddMed.setOnClickListener(v -> replaceFragment(new MedicineAddFragment()));
        
        View btnAddTodo = view.findViewById(R.id.btn_add_todo);
        if (btnAddTodo != null) btnAddTodo.setOnClickListener(v -> replaceFragment(new AddTodoFragment()));
        
        LinearLayout llHeader = view.findViewById(R.id.ll_quick_add_header);
        LinearLayout llOptions = view.findViewById(R.id.ll_quick_add_options);
        if (llHeader != null && llOptions != null) {
            llHeader.setOnClickListener(v -> {
                int vis = llOptions.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
                TransitionManager.beginDelayedTransition((ViewGroup) view);
                llOptions.setVisibility(vis);
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
