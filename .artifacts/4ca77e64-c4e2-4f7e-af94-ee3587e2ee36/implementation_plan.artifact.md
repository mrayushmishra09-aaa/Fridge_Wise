# Implementation Plan - Stability & Performance Fixes

This plan addresses the "App keeps stopping" crashes and UI unresponsiveness reported after the restructuring.

## User Review Required

> [!WARNING]
> I will be removing all "manual" fragment transaction code (e.g., `beginTransaction().replace()...`). All navigation will now strictly use the `Navigation Component`. This is necessary to stop the crashes.

> [!IMPORTANT]
> I will be adding lifecycle checks to background tasks. This means if you are on a slow connection or the database is busy, the app won't crash if you switch screens quickly.

## Proposed Changes

### 1. Unified Navigation (Crash Fix)
#### [MODIFY] [MainActivity.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/ui/activities/MainActivity.java)
- Remove the `savedInstanceState == null` manual transaction logic.
- Ensure `handleIntent` only uses `navController.navigate()`.
- Fix the logic that was causing double-loading of the Home screen.

#### [MODIFY] [Memory.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/ui/fragments/Memory.java)
- Replace all `getParentFragmentManager().beginTransaction()` calls with `Navigation.findNavController(view).navigate()`.
- This applies to: Medicine, Todo, Shopping, Documents, and Custom Space clicks.

### 2. UI Responsiveness (Lag Fix)
#### [MODIFY] [InventoryFragment.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/ui/fragments/InventoryFragment.java)
- Move the search and filtering logic to the background `executor`.
- Add `isAdded()` checks before updating the UI to prevent "Fragment not attached" crashes.

#### [MODIFY] [HomeFragment.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/ui/fragments/HomeFragment.java)
- Add safety checks to the ViewModel observers to handle null states from the AI more gracefully.

### 3. Resource & Path Cleanup
- Verify `nav_graph.xml` destinations match the new package paths.
- Clean up any duplicate imports caused by the automated restructuring.

## Verification Plan

### Automated Tests
- Build the app and check for any remaining "IndexNotReady" or "Cannot resolve symbol" errors.
- Monitor logcat for `IllegalStateException` during navigation.

### Manual Verification
- **Navigation Stress Test**: Rapidly click between Home, Inventory, and Memory to ensure no crashes occur.
- **Deep Link Test**: Trigger a notification and click the action button to verify it navigates to the correct sub-screen without crashing.
- **Search Test**: Type quickly in the Inventory search bar and ensure the UI remains responsive.
