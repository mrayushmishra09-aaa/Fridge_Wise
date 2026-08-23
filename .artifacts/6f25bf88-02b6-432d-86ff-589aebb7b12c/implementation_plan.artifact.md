# Implementation Plan: Home Screen Fixes and Recent Activity Feature

This plan addresses the UI alignment issues on the Home Screen, makes the search bar functional, and implements a unified "Recent Activity" tracking system across all sections (Food, Medicine, Tasks, and Custom Spaces).

## User Review Required

> [!IMPORTANT]
> The "Recent Activity" feature requires a new database table to track user actions globally. This will start tracking from the moment the update is applied.

## Proposed Changes

### UI Enhancement (Home Screen)
Fix the header layout to prevent the logo from shifting when the greeting message is long, and wire up the search bar to navigate to the inventory with results.

#### [MODIFY] [fragment_home.xml](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/res/layout/fragment_home.xml)
- Change the header `LinearLayout` to a `ConstraintLayout` or a more stable horizontal layout.
- Ensure the logo is either centered or stays at a fixed position.
- Add an empty `RecyclerView` container for Recent Activity.

#### [MODIFY] [HomeFragment.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/HomeFragment.java)
- Implement `SearchView.OnQueryTextListener` for `home_searchbar`.
- Navigate to `InventoryFragment` and pass the search query as a bundle argument.
- Initialize the Recent Activity `RecyclerView`.

---

### Data Layer (Recent Activity)
Implement the infrastructure to record and retrieve user actions.

#### [NEW] [ActivityRecord.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/ActivityRecord.java)
- Room Entity with fields: `id`, `type`, `action`, `itemName`, `timestamp`, `iconRes`.

#### [NEW] [ActivityDao.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/ActivityDao.java)
- Methods for `insert` and `getRecentActivities(limit)`.

#### [MODIFY] [AppDatabase.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/AppDatabase.java)
- Register `ActivityRecord` entity and provide `activityDao()`.

---

### ViewModel & Presentation
Fetch and display the activity logs.

#### [MODIFY] [HomeUiState.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/HomeUiState.java)
- Add `List<ActivityRecord> recentActivities` to the state.

#### [MODIFY] [HomeViewModel.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/HomeViewModel.java)
- In `refreshDashboard()`, fetch the latest 5-10 activity records.
- Update `HomeUiState` with the fetched records.

#### [NEW] [RecentActivityAdapter.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/RecentActivityAdapter.java)
- Simple adapter to show activity type, name, and time ago.

---

### Instrumentation (Logging)
Ensure every "Add" action logs an activity record.

#### [MODIFY] [AddItemFragment.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/AddItemFragment.java)
- Log "Food Added/Updated".

#### [MODIFY] [MedicineAddFragment.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/MedicineAddFragment.java)
- Log "Medicine Added/Updated".

#### [MODIFY] [AddTodoFragment.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/AddTodoFragment.java)
- Log "Task Added/Updated".

#### [MODIFY] [AddSpaceItemFragment.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/AddSpaceItemFragment.java)
- Log "Custom Item Added/Updated".

## Verification Plan

### Automated Tests
- N/A (Manual verification on device preferred for UI layout and end-to-end logging).

### Manual Verification
1. **Logo Alignment:** Inject a very long greeting message via `HomeViewModel` (mock or manual change) and verify the logo stays in place.
2. **Search Bar:** Enter a query in the Home search bar, press Enter, and verify navigation to `InventoryFragment` with filtered results.
3. **Recent Activity:**
   - Add a food item.
   - Go back to Home.
   - Verify the "Recent Activity" section shows "Added [Item Name] to Fridge".
   - Repeat for Medicine, Tasks, and Custom Spaces.
