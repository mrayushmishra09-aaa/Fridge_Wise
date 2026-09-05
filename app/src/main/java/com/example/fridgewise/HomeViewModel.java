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
    private final MutableLiveData<String> actionMessage = new MutableLiveData<>();
    private final AppDatabase db;
    private final GeminiManager geminiManager;
    private final PreferenceManager prefManager;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
    
    private static final String[] SMART_TIPS = {
        "Store potatoes and onions separately; they spoil faster if kept together.",
        "Keep milk in the main part of the fridge, not the door, where it's coldest.",
        "Store honey at room temperature; it won't spoil and crystallizes slower.",
        "Put a paper towel with your salad greens to absorb moisture and keep them crisp.",
        "Bread stays fresh longer on the counter than in the fridge where it dries out.",
        "Only move avocados to the fridge once they are fully ripe.",
        "Wrap banana stems in plastic wrap to slow down the ripening process.",
        "Store mushrooms in a paper bag instead of plastic to keep them from getting slimy."
    };
    
    private List<FoodItem> currentActionableItems = new ArrayList<>();

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

    public LiveData<String> getActionMessage() {
        return actionMessage;
    }

    public void refreshDashboard() {
        String userName = prefManager.getUserName();
        HomeUiState current = uiState.getValue();
        if (current == null) {
            uiState.postValue(new HomeUiState(new ArrayList<>(), "Thinking...", "Analyzing fridge...", "Hello!", userName, new ArrayList<>(), new ArrayList<>(), getRandomTip(), false, true));
        }

        executor.execute(() -> {
            ArrayList<AttentionItem> attentionItems = new ArrayList<>();
            ArrayList<FoodItem> lowStockItems = new ArrayList<>();
            ArrayList<FoodItem> expiringSoonItems = new ArrayList<>();
            Calendar calNow = Calendar.getInstance();

            List<ActivityRecord> recentActivities = db.activityDao().getRecentActivities(10);
            if (recentActivities == null) recentActivities = new ArrayList<>();

            List<FoodItem> foodItems = db.foodItemDao().getAllItems();
            Date today = resetTime(calNow.getTime());
            Calendar calSoon = Calendar.getInstance();
            calSoon.add(Calendar.DAY_OF_YEAR, 3);
            Date soonDate = resetTime(calSoon.getTime());

            for (FoodItem item : foodItems) {
                if (item.getQuantity() <= 1) lowStockItems.add(item);

                try {
                    Date expiry = dateFormat.parse(item.getExpiryDate());
                    if (expiry != null) {
                        expiry = resetTime(expiry);
                        if (expiry.before(today)) {
                            AttentionItem ai = new AttentionItem(String.valueOf(item.getId()), item.getName(), "Expired", "In Fridge", "Has expired!", "View", AttentionItem.Type.FOOD);
                            ai.setPriorityScore(10);
                            ai.setImageResId(CategoryUtils.getCategoryIcon(item.getCategory()));
                            attentionItems.add(ai);
                        } else if (expiry.equals(today)) {
                            expiringSoonItems.add(item);
                            AttentionItem ai = new AttentionItem(String.valueOf(item.getId()), item.getName(), "Expires today", "In Fridge", "Use today!", "View", AttentionItem.Type.FOOD);
                            ai.setPriorityScore(50);
                            ai.setImageResId(CategoryUtils.getCategoryIcon(item.getCategory()));
                            attentionItems.add(ai);
                        } else if (expiry.before(soonDate)) {
                            expiringSoonItems.add(item);
                        }
                    }
                } catch (ParseException e) { }
            }

            // Meds & Tasks
            List<MedicineEntity> medicines = db.medicineDao().getAllMedicines();
            String todayStr = dateFormat.format(calNow.getTime());
            int totalMedsToday = medicines.size();
            int takenMedsToday = 0;
            
            for (MedicineEntity med : medicines) {
                boolean isTaken = todayStr.equals(med.getLastTakenDate());
                if (isTaken) takenMedsToday++;
                
                if (med.isReminderOn() && !isTaken) {
                    AttentionItem ai = new AttentionItem(String.valueOf(med.getId()), med.getMedicineName(), med.getStartTime(), "Medicine", "Time for meds.", "View", AttentionItem.Type.MEDICINE);
                    ai.setPriorityScore(100);
                    ai.setImageResId(med.getIconResId());
                    attentionItems.add(ai);
                }
            }

            List<TodoItem> todos = db.todoDao().getPendingTodos();
            for (TodoItem todo : todos) {
                if ("High".equalsIgnoreCase(todo.getPriority())) {
                    AttentionItem ai = new AttentionItem(String.valueOf(todo.getId()), todo.getTitle(), todo.getTime() != null ? todo.getTime() : "Today", "To-Do", "High priority task.", "View", AttentionItem.Type.TODO);
                    ai.setPriorityScore(80);
                    ai.setImageResId(R.drawable.ic_todo_item);
                    attentionItems.add(ai);
                }
            }

            int shoppingCount = db.shoppingDao().getAllItems().size();

            attentionItems.sort((a, b) -> Long.compare(b.getPriorityScore(), a.getPriorityScore()));

            List<FoodItem> combined = new ArrayList<>(lowStockItems);
            for (FoodItem fi : expiringSoonItems) { if (!combined.contains(fi)) combined.add(fi); }
            if (combined.size() > 5) combined = combined.subList(0, 5);
            currentActionableItems = combined;
            boolean hasActionable = !currentActionableItems.isEmpty();
            final List<ActivityRecord> activities = recentActivities;
            
            final int fTotalMeds = totalMedsToday;
            final int fTakenMeds = takenMedsToday;
            final int fTodoCount = todos.size();
            final int fShoppingCount = shoppingCount;

            if (!expiringSoonItems.isEmpty()) {
                StringBuilder ingredients = new StringBuilder();
                for (FoodItem fi : expiringSoonItems) ingredients.append(fi.getName()).append(", ");
                geminiManager.getRecipeSuggestions(ingredients.toString(), new GeminiManager.RecipeCallback() {
                    @Override
                    public void onRecipesGenerated(List<RecipeItem> recipes) {
                        fetchInsight(attentionItems, activities, hasActionable, recipes, fTotalMeds, fTakenMeds, fTodoCount, fShoppingCount);
                    }
                    @Override
                    public void onError(Throwable t) {
                        fetchInsight(attentionItems, activities, hasActionable, new ArrayList<>(), fTotalMeds, fTakenMeds, fTodoCount, fShoppingCount);
                    }
                });
            } else {
                fetchInsight(attentionItems, activities, hasActionable, new ArrayList<>(), fTotalMeds, fTakenMeds, fTodoCount, fShoppingCount);
            }
        });
    }

    private void fetchInsight(List<AttentionItem> attentionItems, List<ActivityRecord> activities, boolean hasActionable, List<RecipeItem> recipes, 
                              int totalMeds, int takenMeds, int todoCount, int shoppingCount) {
        String userName = prefManager.getUserName();
        Calendar calNow = Calendar.getInstance();
        int hour = calNow.get(Calendar.HOUR_OF_DAY);
        String timeOfDay = (hour >= 5 && hour < 12) ? "Morning" : (hour >= 12 && hour < 17) ? "Afternoon" : (hour >= 17 && hour < 21) ? "Evening" : "Night";
        String fallbackGreeting = "Good " + timeOfDay + "!";

        StringBuilder data = new StringBuilder("User: " + userName + ". Time: " + timeOfDay + ". ");
        data.append("Food: " + (hasActionable ? currentActionableItems.size() + " urgent" : "stocked") + ". ");
        data.append("Meds: " + takenMeds + " of " + totalMeds + " doses taken. ");
        data.append("Tasks: " + todoCount + " pending. ");
        data.append("Shopping: " + shoppingCount + " items on list. ");

        geminiManager.getSmartInsight(data.toString(), new GeminiManager.InsightCallback() {
            @Override
            public void onInsightGenerated(String greeting, String title, String description) {
                uiState.postValue(new HomeUiState(attentionItems, title, description, greeting, userName, activities, recipes, getRandomTip(), hasActionable, false));
            }
            @Override
            public void onError(Throwable t) {
                uiState.postValue(new HomeUiState(attentionItems, "Fridge Insight", "Everything is looking good today!", fallbackGreeting, userName, activities, recipes, getRandomTip(), hasActionable, false));
            }
        });
    }

    private String getRandomTip() {
        int index = (int) (Math.random() * SMART_TIPS.length);
        return SMART_TIPS[index];
    }

    public void autoAddActionableToShoppingList() {
        if (currentActionableItems.isEmpty()) return;
        executor.execute(() -> {
            List<ShoppingItem> existingShopping = db.shoppingDao().getAllItems();
            int addedCount = 0;
            for (FoodItem food : currentActionableItems) {
                boolean alreadyInList = false;
                for (ShoppingItem shop : existingShopping) {
                    if (shop.getName().equalsIgnoreCase(food.getName())) { alreadyInList = true; break; }
                }
                if (!alreadyInList) {
                    db.shoppingDao().insert(new ShoppingItem(food.getName(), "1", food.getUnit(), false));
                    addedCount++;
                }
            }
            String message = addedCount > 0 ? "Added " + addedCount + " items to list!" : "Items already in list.";
            actionMessage.postValue(message);
            refreshDashboard();
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
