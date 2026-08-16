package com.example.fridgewise;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link homeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class homeFragment extends Fragment {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    public homeFragment() {
        // Required empty public constructor
    }

    public static homeFragment newInstance() {
        return new homeFragment();
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
        TextView tvTotalItems = view.findViewById(R.id.total_items_numtext);
        TextView tvExpiringSoon = view.findViewById(R.id.expiring_soon_numview);
        TextView tvExpiredItems = view.findViewById(R.id.rotten_items_numview);
        
        TextView tvFoodInsight = view.findViewById(R.id.tv_food_insight);
        TextView tvMedInsight = view.findViewById(R.id.tv_medicine_insight);
        TextView tvTodoInsight = view.findViewById(R.id.tv_todo_insight);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            
            // Food Stats & Insight
            List<FoodItem> foodItems = db.foodItemDao().getAllItems();
            int total = foodItems.size();
            int expiringSoonCount = 0;
            int expiredCount = 0;
            String foodInsightMessage = "Your fridge looks good! No items expiring soon.";
            
            Calendar cal = Calendar.getInstance();
            Date today = resetTime(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, 3);
            Date threeDaysFromNow = resetTime(cal.getTime());

            for (FoodItem item : foodItems) {
                try {
                    Date expiry = dateFormat.parse(item.getExpiryDate());
                    if (expiry != null) {
                        expiry = resetTime(expiry);
                        if (expiry.before(today)) {
                            expiredCount++;
                            foodInsightMessage = "Alert: " + item.getName() + " has expired! Clean it out.";
                        } else if (expiry.equals(today) || (expiry.after(today) && expiry.before(threeDaysFromNow))) {
                            expiringSoonCount++;
                            if (expiry.equals(today)) {
                                foodInsightMessage = "Today's Insight: " + item.getName() + " expires today. Use it!";
                            } else if (foodInsightMessage.startsWith("Your fridge")) {
                                foodInsightMessage = "Insight: " + item.getName() + " will expire soon.";
                            }
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            // Medicine Insight
            List<MedicineEntity> medicines = db.medicineDao().getAllMedicines();
            String medInsightMessage = "No medicines scheduled for today.";
            if (!medicines.isEmpty()) {
                int reminderCount = 0;
                for (MedicineEntity med : medicines) {
                    if (med.isReminderOn()) reminderCount++;
                }
                if (reminderCount > 0) {
                    medInsightMessage = "Reminder: You have " + reminderCount + " active medicine schedules.";
                }
            }

            // To-Do Insight
            List<TodoItem> pendingTodos = db.todoDao().getPendingTodos();
            String todoInsightMessage = "No pending tasks for today.";
            if (!pendingTodos.isEmpty()) {
                todoInsightMessage = "Task: You have " + pendingTodos.size() + " items on your to-do list.";
            }

            final int fTotal = total;
            final int fSoon = expiringSoonCount;
            final int fExpired = expiredCount;
            final String fFoodMsg = foodInsightMessage;
            final String fMedMsg = medInsightMessage;
            final String fTodoMsg = todoInsightMessage;

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (tvTotalItems != null) tvTotalItems.setText(String.valueOf(fTotal));
                    if (tvExpiringSoon != null) tvExpiringSoon.setText(String.valueOf(fSoon));
                    if (tvExpiredItems != null) tvExpiredItems.setText(String.valueOf(fExpired));
                    
                    if (tvFoodInsight != null) tvFoodInsight.setText(fFoodMsg);
                    if (tvMedInsight != null) tvMedInsight.setText(fMedMsg);
                    if (tvTodoInsight != null) tvTodoInsight.setText(fTodoMsg);
                });
            }
        }).start();
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
        view.findViewById(R.id.qa_ca01).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView2, new AddItemFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Search Bar Logic (placeholder)
        view.findViewById(R.id.search_bar).setOnClickListener(v -> {
            // Future search implementation
        });

        // Shopping List (placeholder - for now let's just go to Inventory or alerts)
        view.findViewById(R.id.qa_cv02).setOnClickListener(v -> {
             // Scan items
        });

        view.findViewById(R.id.qa_cv04).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView2, new alertsFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}