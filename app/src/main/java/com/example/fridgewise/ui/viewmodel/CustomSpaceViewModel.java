package com.example.fridgewise.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.fridgewise.data.AppDatabase;
import com.example.fridgewise.model.CustomSpace;
import com.example.fridgewise.model.CustomSpaceItem;
import com.example.fridgewise.util.NotificationHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CustomSpaceViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Integer> spaceId = new MutableLiveData<>();
    private final LiveData<List<CustomSpaceItem>> items;
    private final LiveData<CustomSpace> currentSpace;
    private final MutableLiveData<Set<Integer>> selectedIds = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Boolean> isSelectionMode = new MutableLiveData<>(false);

    public CustomSpaceViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        
        currentSpace = Transformations.switchMap(spaceId, id -> 
            db.customSpaceDao().getSpaceById(id));
            
        items = Transformations.switchMap(spaceId, id -> 
            db.customSpaceDao().getItemsForSpace(id));
    }

    public void setSpaceId(int id) {
        spaceId.setValue(id);
    }

    public LiveData<List<CustomSpaceItem>> getItems() {
        return items;
    }

    public LiveData<CustomSpace> getCurrentSpace() {
        return currentSpace;
    }

    public LiveData<Set<Integer>> getSelectedIds() {
        return selectedIds;
    }

    public LiveData<Boolean> getIsSelectionMode() {
        return isSelectionMode;
    }

    public void toggleSelection(int id) {
        Set<Integer> current = selectedIds.getValue();
        if (current == null) current = new HashSet<>();
        
        if (current.contains(id)) {
            current.remove(id);
        } else {
            current.add(id);
        }
        selectedIds.setValue(current);
        
        if (current.isEmpty()) {
            isSelectionMode.setValue(false);
        }
    }

    public void enterSelectionMode(int firstId) {
        isSelectionMode.setValue(true);
        Set<Integer> set = new HashSet<>();
        set.add(firstId);
        selectedIds.setValue(set);
    }

    public void selectAll(List<CustomSpaceItem> allItems) {
        Set<Integer> set = new HashSet<>();
        for (CustomSpaceItem item : allItems) {
            set.add(item.getId());
        }
        selectedIds.setValue(set);
        isSelectionMode.setValue(true);
    }

    public void deselectAll() {
        selectedIds.setValue(new HashSet<>());
        isSelectionMode.setValue(false);
    }

    public void exitSelectionMode() {
        isSelectionMode.setValue(false);
        selectedIds.setValue(new HashSet<>());
    }

    public void deleteSelectedItems() {
        Set<Integer> idsToDelete = selectedIds.getValue();
        if (idsToDelete == null || idsToDelete.isEmpty()) return;
        
        executor.execute(() -> {
            // Cancel notifications before deleting
            for (Integer id : idsToDelete) {
                int notificationId = NotificationHelper.generateId("SPACE", id);
                NotificationHelper.cancelNotification(getApplication(), notificationId);
            }
            db.customSpaceDao().deleteItemsByIds(new ArrayList<>(idsToDelete));
            requireActivityOnUiThread(() -> exitSelectionMode());
        });
    }

    private void requireActivityOnUiThread(Runnable runnable) {
        // Since we are in ViewModel, we can't easily get Activity. 
        // But LiveData updates on main thread anyway if we use setValue.
        // If we use postValue, it's safe from background.
        isSelectionMode.postValue(false);
        selectedIds.postValue(new HashSet<>());
    }

    public void updateItem(CustomSpaceItem item) {
        executor.execute(() -> db.customSpaceDao().updateItem(item));
    }

    public void deleteItem(CustomSpaceItem item) {
        executor.execute(() -> {
            db.customSpaceDao().deleteItem(item);
            int notificationId = NotificationHelper.generateId("SPACE", item.getId());
            NotificationHelper.cancelNotification(getApplication(), notificationId);
        });
    }

    public void deleteSpace(CustomSpace space) {
        executor.execute(() -> {
            // Delete all items first (or rely on Cascade if configured, but we do it manually)
            List<CustomSpaceItem> currentItems = db.customSpaceDao().getAllCustomSpaceItemsSync();
            for (CustomSpaceItem item : currentItems) {
                if (item.getSpaceId() == space.getId()) {
                    db.customSpaceDao().deleteItem(item);
                    int notificationId = NotificationHelper.generateId("SPACE", item.getId());
                    NotificationHelper.cancelNotification(getApplication(), notificationId);
                }
            }
            db.customSpaceDao().deleteSpace(space);
        });
    }

    public void autoRemoveExpiredItems(int durationDays) {
        if (durationDays <= 0) return;
        
        executor.execute(() -> {
            List<CustomSpaceItem> allItems = db.customSpaceDao().getAllCustomSpaceItemsSync();
            long now = System.currentTimeMillis();
            long durationMillis = durationDays * 24L * 60L * 60L * 1000L;
            
            for (CustomSpaceItem item : allItems) {
                if (item.getSpaceId() == spaceId.getValue() && item.isChecked() && item.getCompletionTimestamp() != null) {
                    if (now - item.getCompletionTimestamp() > durationMillis) {
                        db.customSpaceDao().deleteItem(item);
                        int notificationId = NotificationHelper.generateId("SPACE", item.getId());
                        NotificationHelper.cancelNotification(getApplication(), notificationId);
                    }
                }
            }
        });
    }
}
