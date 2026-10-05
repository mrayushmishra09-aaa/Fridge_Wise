package com.example.fridgewise.ui.viewmodel;

import com.example.fridgewise.R;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fridgewise.BuildConfig;
import com.example.fridgewise.data.AppDatabase;
import com.example.fridgewise.data.GeminiManager;
import com.example.fridgewise.data.PreferenceManager;
import com.example.fridgewise.model.ActivityRecord;
import com.example.fridgewise.model.AttentionItem;
import com.example.fridgewise.model.CustomSpace;
import com.example.fridgewise.model.CustomSpaceItem;
import com.example.fridgewise.model.FoodItem;
import com.example.fridgewise.model.HomeUiState;
import com.example.fridgewise.model.MedicineEntity;
import com.example.fridgewise.model.ShoppingItem;
import com.example.fridgewise.model.TodoItem;
import com.example.fridgewise.util.CategoryUtils;
import com.example.fridgewise.util.UniversalInputParser;

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
            uiState.postValue(new HomeUiState(new ArrayList<>(), "Thinking...", "Analyzing fridge...", "Hello!", userName, new ArrayList<>(), false, true));
        } else {
            uiState.postValue(new HomeUiState(
                current.attentionItems,
                current.insightTitle,
                current.insightDescription,
                current.greeting,
                current.userName,
                current.recentActivities,
                current.hasActionableItems,
                true
            ));
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
                        long targetTs = item.getExpiryTimestamp() > 0 ? item.getExpiryTimestamp() : getEndOfDayTimestamp(expiry);
                        if (expiry.before(today)) {
                            long diffDays = (today.getTime() - expiry.getTime()) / (24 * 60 * 60 * 1000);
                            if (diffDays <= 7) {
                                AttentionItem ai = new AttentionItem(String.valueOf(item.getId()), item.getName(), "Expired", item.getCategory(), "Has expired!", "View", AttentionItem.Type.FOOD);
                                ai.setPriorityScore(10 + (7 - (int)diffDays));
                                ai.setTargetTimestamp(targetTs);
                                ai.setImageResId(CategoryUtils.getCategoryIcon(item.getCategory()));
                                ai.setBadgeTextColor(ContextCompat.getColor(getApplication(), R.color.badge_red_text));
                                ai.setStatusColor(ContextCompat.getColor(getApplication(), R.color.red_expired));
                                attentionItems.add(ai);
                            }
                        } else if (expiry.equals(today)) {
                            expiringSoonItems.add(item);
                            AttentionItem ai = new AttentionItem(String.valueOf(item.getId()), item.getName(), "Expires today", item.getCategory(), "Use today!", "View", AttentionItem.Type.FOOD);
                            ai.setPriorityScore(50);
                            ai.setTargetTimestamp(targetTs);
                            ai.setImageResId(CategoryUtils.getCategoryIcon(item.getCategory()));
                            ai.setBadgeTextColor(ContextCompat.getColor(getApplication(), R.color.attention_badge_orange_text));
                            ai.setStatusColor(ContextCompat.getColor(getApplication(), R.color.attention_badge_orange_text));
                            attentionItems.add(ai);
                        } else if (expiry.before(soonDate)) {
                            expiringSoonItems.add(item);
                            long diff = (expiry.getTime() - today.getTime()) / (24 * 60 * 60 * 1000);
                            String badgeText = diff == 1 ? "Expires tomorrow" : "Expires in " + diff + " days";
                            AttentionItem ai = new AttentionItem(String.valueOf(item.getId()), item.getName(), badgeText, item.getCategory(), "Use soon.", "View", AttentionItem.Type.FOOD);
                            ai.setPriorityScore(30);
                            ai.setTargetTimestamp(targetTs);
                            ai.setImageResId(CategoryUtils.getCategoryIcon(item.getCategory()));
                            ai.setBadgeTextColor(ContextCompat.getColor(getApplication(), R.color.attention_badge_orange_text));
                            ai.setStatusColor(ContextCompat.getColor(getApplication(), R.color.attention_badge_orange_text));
                            attentionItems.add(ai);
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
                    ai.setTargetTimestamp(parseMedicineTimestamp(med.getStartTime()));
                    ai.setImageResId(med.getIconResId());
                    ai.setBadgeBgColor(ContextCompat.getColor(getApplication(), R.color.badge_purple_bg));
                    ai.setBadgeTextColor(ContextCompat.getColor(getApplication(), R.color.badge_purple_text));
                    ai.setStatusColor(ContextCompat.getColor(getApplication(), R.color.purple_primary));
                    attentionItems.add(ai);
                }
            }

            List<TodoItem> todos = db.todoDao().getPendingTodos();
            for (TodoItem todo : todos) {
                int score = 0;
                String description = "";
                if ("High".equalsIgnoreCase(todo.getPriority())) {
                    score = 80;
                    description = "High priority task.";
                } else if ("Medium".equalsIgnoreCase(todo.getPriority())) {
                    score = 60;
                    description = "Medium priority task.";
                } else if ("Low".equalsIgnoreCase(todo.getPriority())) {
                    score = 40;
                    description = "Low priority task.";
                } else {
                    score = 20;
                    description = "Task reminder.";
                }

                AttentionItem ai = new AttentionItem(String.valueOf(todo.getId()), todo.getTitle(), todo.getTime() != null ? todo.getTime() : "Today", "To-Do", description, "View", AttentionItem.Type.TODO);
                ai.setPriorityScore(score);
                long targetTs = parseTodoTimestamp(todo.getDate(), todo.getTime());
                long creationTs = todo.getStatusChangeTime() > 0 ? todo.getStatusChangeTime() : System.currentTimeMillis();
                if (creationTs >= targetTs) {
                    creationTs = targetTs - (30 * 60 * 1000L);
                }
                ai.setTargetTimestamp(targetTs);
                ai.setCreationTimestamp(creationTs);
                ai.setImageResId(CategoryUtils.getPriorityIcon(todo.getPriority()));
                ai.setBadgeBgColor(ContextCompat.getColor(getApplication(), R.color.card_blue));
                ai.setBadgeTextColor(ContextCompat.getColor(getApplication(), R.color.doc_primary));
                ai.setStatusColor(ContextCompat.getColor(getApplication(), R.color.doc_primary));
                attentionItems.add(ai);
            }

            // Custom Spaces Items
            try {
                List<CustomSpaceItem> spaceItems = db.customSpaceDao().getAllCustomSpaceItemsSync();
                for (CustomSpaceItem spaceItem : spaceItems) {
                    if (spaceItem.isChecked()) continue;

                    CustomSpace parentSpace = db.customSpaceDao().getSpaceByIdSync(spaceItem.getSpaceId());
                    String spaceName = parentSpace != null ? parentSpace.getName() : "Custom Space";

                    Long targetTs = spaceItem.getReminderTimestamp();
                    String badgeText = "Space";
                    String hintText = spaceItem.getNotes() != null && !spaceItem.getNotes().isEmpty() ? spaceItem.getNotes() : "Space Item";

                    SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    if (targetTs != null && targetTs > 0) {
                        badgeText = timeFmt.format(new Date(targetTs));
                    } else if (spaceItem.getDate() != null && !spaceItem.getDate().trim().isEmpty()) {
                        targetTs = parseTodoTimestamp(spaceItem.getDate(), "23:59");
                        badgeText = spaceItem.getDate();
                    }

                    if (targetTs != null && targetTs > 0) {
                        long now = System.currentTimeMillis();
                        long diffDays = (targetTs - now) / (24 * 60 * 60 * 1000L);
                        if (targetTs <= now || diffDays <= 3) {
                            AttentionItem ai = new AttentionItem(
                                parentSpace != null ? parentSpace.getId() + "_" + spaceItem.getId() : String.valueOf(spaceItem.getId()),
                                spaceItem.getName(),
                                badgeText,
                                spaceName,
                                hintText,
                                "View",
                                AttentionItem.Type.SPACE
                            );
                            ai.setPriorityScore(70);
                            ai.setTargetTimestamp(targetTs);
                            long creationTs = spaceItem.getCompletionTimestamp() != null && spaceItem.getCompletionTimestamp() > 0 
                                ? spaceItem.getCompletionTimestamp() 
                                : System.currentTimeMillis();
                            if (creationTs >= targetTs) {
                                creationTs = targetTs - (30 * 60 * 1000L);
                            }
                            ai.setCreationTimestamp(creationTs);
                            ai.setImageResId(R.drawable.ic_nav_spaces);
                            ai.setBadgeBgColor(ContextCompat.getColor(getApplication(), R.color.badge_purple_bg));
                            ai.setBadgeTextColor(ContextCompat.getColor(getApplication(), R.color.badge_purple_text));
                            ai.setStatusColor(ContextCompat.getColor(getApplication(), R.color.purple_primary));
                            attentionItems.add(ai);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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

            fetchInsight(attentionItems, activities, hasActionable, fTotalMeds, fTakenMeds, fTodoCount, fShoppingCount);
        });
    }

    private void fetchInsight(List<AttentionItem> attentionItems, List<ActivityRecord> activities, boolean hasActionable, 
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
                uiState.postValue(new HomeUiState(attentionItems, title, description, greeting, userName, activities, hasActionable, false));
            }
            @Override
            public void onError(Throwable t) {
                uiState.postValue(new HomeUiState(attentionItems, "Fridge Insight", "Everything is looking good today!", fallbackGreeting, userName, activities, hasActionable, false));
            }
        });
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

    public void processUniversalInput(String input) {
        UniversalInputParser.ParsedResult result = UniversalInputParser.parse(input);
        if (result == null || !result.isValid) {
            actionMessage.postValue("Invalid input format.");
            return;
        }

        executor.execute(() -> {
            try {
                boolean success = false;
                String itemName = "";
                int iconRes = R.drawable.ic_sparkle;

                switch (result.section) {
                    case "task":
                        TodoItem todo = new TodoItem();
                        todo.setTitle(result.get("name", "New Task"));
                        todo.setPriority(result.get("priority", "Medium"));
                        todo.setTime(result.get("time", "9:00 AM"));
                        todo.setDate(result.get("date", dateFormat.format(new Date())));
                        todo.setNote(result.get("note", ""));
                        todo.setCompleted(false);
                        db.todoDao().insert(todo);
                        itemName = todo.getTitle();
                        iconRes = CategoryUtils.getPriorityIcon(todo.getPriority());
                        success = true;
                        break;

                    case "shop":
                        ShoppingItem shop = new ShoppingItem();
                        shop.setName(result.get("item", "Item"));
                        shop.setQuantity(result.get("quantity", "1"));
                        shop.setUnit(result.get("unit", "pcs"));
                        shop.setCompleted(false);
                        db.shoppingDao().insert(shop);
                        itemName = shop.getName();
                        iconRes = R.drawable.v02_img_icons_shopping;
                        success = true;
                        break;

                    case "med":
                        MedicineEntity med = new MedicineEntity();
                        med.setMedicineName(result.get("name", "Medicine"));
                        med.setMedicineType(result.get("type", "Pill"));
                        med.setStartTime(result.get("time", "9:00 AM"));
                        med.setStartDate(result.get("date", dateFormat.format(new Date())));
                        med.setDosage(result.get("dosage", "1"));
                        db.medicineDao().insert(med);
                        itemName = med.getMedicineName();
                        iconRes = R.drawable.med_image_07;
                        success = true;
                        break;

                    case "inv":
                        FoodItem food = new FoodItem();
                        food.setName(result.get("item", "Food"));
                        food.setCategory(result.get("category", "Others"));
                        try {
                            food.setQuantity(Double.parseDouble(result.get("quantity", "1")));
                        } catch (NumberFormatException e) {
                            food.setQuantity(1.0);
                        }
                        food.setUnit(result.get("unit", "pcs"));
                        food.setExpiryDate(result.get("expiry", dateFormat.format(new Date())));
                        food.setPurchaseDate(dateFormat.format(new Date()));
                        db.foodItemDao().insert(food);
                        itemName = food.getName();
                        iconRes = CategoryUtils.getCategoryIcon(food.getCategory());
                        success = true;
                        break;
                }

                if (success) {
                    db.activityDao().insert(new ActivityRecord(
                            result.section.toUpperCase(),
                            "Added",
                            itemName,
                            System.currentTimeMillis(),
                            iconRes
                    ));
                    actionMessage.postValue("Successfully added " + itemName);
                    refreshDashboard();
                }

            } catch (Exception e) {
                e.printStackTrace();
                actionMessage.postValue("Error saving data: " + e.getMessage());
            }
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

    private long getEndOfDayTimestamp(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    private long parseMedicineTimestamp(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return System.currentTimeMillis() + 3600000L;
        String todayDateStr = dateFormat.format(new Date());
        String combined = todayDateStr + " " + timeStr.trim();
        String[] patterns = new String[] { "d/M/yyyy h:mm a", "d/M/yyyy hh:mm a", "d/M/yyyy HH:mm" };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
                Date parsed = sdf.parse(combined);
                if (parsed != null) return parsed.getTime();
            } catch (Exception ignored) { }
        }
        return System.currentTimeMillis() + 3600000L;
    }

    private long parseTodoTimestamp(String dateStr, String timeStr) {
        String trimmedDate = (dateStr == null || dateStr.trim().isEmpty()) ? dateFormat.format(new Date()) : dateStr.trim();
        String trimmedTime = (timeStr != null && !timeStr.trim().isEmpty()) ? timeStr.trim() : "23:59";
        String combined = trimmedDate + " " + trimmedTime;

        String[] patterns = new String[] {
            "d/M/yyyy h:mm a",
            "d/M/yyyy hh:mm a",
            "d/M/yyyy HH:mm",
            "dd/MM/yyyy h:mm a",
            "dd/MM/yyyy HH:mm",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd h:mm a"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
                Date parsed = sdf.parse(combined);
                if (parsed != null) {
                    return parsed.getTime();
                }
            } catch (Exception ignored) { }
        }

        String[] datePatterns = new String[] { "d/M/yyyy", "dd/MM/yyyy", "yyyy-MM-dd" };
        for (String pattern : datePatterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
                Date parsedDate = sdf.parse(trimmedDate);
                if (parsedDate != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(parsedDate);
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    return cal.getTimeInMillis();
                }
            } catch (Exception ignored) { }
        }

        return System.currentTimeMillis() + 86400000L;
    }
}
