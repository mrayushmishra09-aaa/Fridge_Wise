# Implementation Plan - Bug Fix Marathon

We will fix the five identified bugs one by one, starting from the most critical (data loss) to UI optimizations.

## User Review Required

> [!IMPORTANT]
> **Database Reset**: To fix the Food Sorting bug correctly, I will transition date storage from strings to numeric timestamps. Because the project uses `fallbackToDestructiveMigration()`, **all existing data (food, meds, tasks) will be cleared** upon the first launch after this fix. Please backup any important test data before proceeding.

## Proposed Changes

### 1. Fix Medicine Deletion (Data Loss)
Currently, `CleanupWorker` deletes medicine records 24h after they are taken. This prevents recurring reminders.
#### [MODIFY] [CleanupWorker.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/CleanupWorker.java)
- Change logic to clear `lastTakenDate` instead of calling `db.medicineDao().delete(med)`.

---

### 2. Fix Food Sorting (Logic Error)
Dates like "10/12/2026" sort before "2/12/2026" because they are strings.
#### [MODIFY] [FoodItem.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/FoodItem.java)
- Add a new field `expiryTimestamp` (Long) to store the date as a number.
#### [MODIFY] [FoodItemDao.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/FoodItemDao.java)
- Update query to `ORDER BY expiryTimestamp ASC`.
#### [MODIFY] [AddItemFragment.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/AddItemFragment.java) (and others)
- Ensure `expiryTimestamp` is saved whenever `expiryDate` is set.

---

### 3. Fix Stale Search Results (UI Refresh)
Search data doesn't update if you edit an item and return.
#### [MODIFY] [GlobalSearchFragment.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/GlobalSearchFragment.java)
- Move `loadAllData()` and `performSearch()` logic into `onResume()` or use a refresh mechanism when returning from the backstack.

---

### 4. Remove Destructive Migration (Data Safety)
#### [MODIFY] [AppDatabase.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/AppDatabase.java)
- Increment version and remove `fallbackToDestructiveMigration()`. (Note: I will only do this after Fix 2 is applied to avoid multiple wipes).

---

### 5. Fix Redundant Startup Loading (Performance)
#### [MODIFY] [MainActivity.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/MainActivity.java)
- Refactor `onCreate` to avoid calling `handleIntent` if a fragment is already being restored by the system.

## Verification Plan

### Automated Tests
- Run `:app:assembleDebug` after each fix to ensure no syntax errors.

### Manual Verification
1. **Medicine**: Mark a medicine as taken, wait (or spoof time) and verify it resets its status rather than disappearing.
2. **Sorting**: Add food with dates "2/12/2026" and "10/12/2026". Verify "2/12" appears first.
3. **Search**: Search for an item -> Edit it -> Press back -> Verify search shows updated name.
