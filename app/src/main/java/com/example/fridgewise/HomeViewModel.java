package com.example.fridgewise;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HomeViewModel extends AndroidViewModel {

    private final MutableLiveData<HomeUiState> uiState = new MutableLiveData<>();
    private final AppDatabase db;
    private final GeminiManager geminiManager;
    private final PreferenceManager prefManager;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    public HomeViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        geminiManager = new GeminiManager(BuildConfig.GEMINI_API_KEY);
        prefManager = new PreferenceManager(application);
        refreshDashboard();
    }

    public LiveData<HomeUiState> getUiState() {
        return uiState;
    }

    public void refreshDashboard() {
        String userName = prefManager.getUserName();
        // Post initial loading state if needed
        HomeUiState current = uiState.getValue();
        if (current == null) {
            uiState.postValue(new HomeUiState(new ArrayList<>(), "Thinking...", "Analyzing your fridge...", "Hello!", userName, new ArrayList<>(), true));
        }

        executor.execute(() -> {
            ArrayList<AttentionItem> attentionItems = new ArrayList<>();
            Calendar calNow = Calendar.getInstance();

            // 0. Recent Activities
            List<ActivityRecord> recentActivities = db.activityDao().getRecentActivities(10);
            if (recentActivities == null) recentActivities = new ArrayList<>();

            // 1. Food Data
            List<FoodItem> foodItems = db.foodItemDao().getAllItems();
            Date today = resetTime(calNow.getTime());

            String fallbackInsightTitle = null;
            String fallbackInsightDesc = null;

            for (FoodItem item : foodItems) {
                if (fallbackInsightTitle == null && item.getQuantity() <= 1) {
                    fallbackInsightTitle = item.getName() + " is running low";
                    fallbackInsightDesc = "You have very little " + item.getName() + " left. Add to shopping list?";
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
            int pendingMedCount = 0;
            String todayStr = dateFormat.format(calNow.getTime());
            for (MedicineEntity med : medicines) {
                if (med.isReminderOn()) {
                    activeMedCount++;
                    // Only add to attention if NOT taken today
                    if (!todayStr.equals(med.getLastTakenDate())) {
                        pendingMedCount++;
                        AttentionItem ai = new AttentionItem(String.valueOf(med.getId()), med.getMedicineName(), med.getStartTime(), "Medicine", "Time to take your meds.", "View", AttentionItem.Type.MEDICINE);
                        ai.setPriorityScore(100);
                        ai.setImageResId(med.getIconResId());
                        attentionItems.add(ai);
                    }
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

            // Time-based fallback greeting
            String timeOfDay;
            int hour = calNow.get(Calendar.HOUR_OF_DAY);
            if (hour >= 5 && hour < 12) timeOfDay = "Morning";
            else if (hour >= 12 && hour < 17) timeOfDay = "Afternoon";
            else if (hour >= 17 && hour < 21) timeOfDay = "Evening";
            else timeOfDay = "Night";
            String fallbackGreeting = "Good " + timeOfDay + "!";

            // AI Insight
            StringBuilder data = new StringBuilder("Time: " + timeOfDay + ". ");
            data.append("Food count: ").append(foodItems.size()).append(". ");
            data.append("Meds pending: ").append(pendingMedCount).append(" out of ").append(activeMedCount).append(". ");
            data.append("High Priority Tasks: ").append(pendingTodos.size()).append(".");

            final String fTitle = fallbackInsightTitle;
            final String fDesc = fallbackInsightDesc;
            final List<ActivityRecord> activities = recentActivities;

            geminiManager.getSmartInsight(data.toString(), new GeminiManager.InsightCallback() {
                @Override
                public void onInsightGenerated(String greeting, String title, String description) {
                    uiState.postValue(new HomeUiState(attentionItems, title, description, greeting, userName, activities, false));
                }

                @Override
                public void onError(Throwable t) {
                    String title = fTitle != null ? fTitle : "Welcome to FridgeWise!";
                    String desc = fDesc != null ? fDesc : "Add your first item to start getting smart insights.";
                    uiState.postValue(new HomeUiState(attentionItems, title, desc, fallbackGreeting, userName, activities, false));
                }
            });
        });
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
}
