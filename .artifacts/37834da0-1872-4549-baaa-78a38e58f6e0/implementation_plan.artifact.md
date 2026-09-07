# Implementation Plan - Stability & Navigation Fixes

This plan addresses the reported "App keeps stopping" issues (crashes) and ensures the app runs smoothly by fixing navigation conflicts, resource issues, and lifecycle-unsafe UI updates.

## User Review Required

> [!IMPORTANT]
> I will be removing all manual fragment transactions (`beginTransaction()`) and replacing them with the `Navigation Component`. This is critical because mixing manual transactions with the Navigation Component causes inconsistent backstack states and frequent crashes.

> [!WARNING]
> I will be correcting some invalid XML attributes (like `style="bold"` on a `ConstraintLayout`) which can cause inflation errors on certain Android versions.

## Proposed Changes

### 1. Navigation & Stability Fixes
#### [MODIFY] [TodoListFragment.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/ui/fragments/TodoListFragment.java)
- Replace manual fragment transactions for adding and editing tasks with `navController.navigate()`.
- Clean up duplicate `R` imports.
- Fix year format inconsistency in date parsing.

#### [MODIFY] [MainActivity.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/ui/activities/MainActivity.java)
- Remove redundant duplicate imports.
- Ensure `handleIntent` doesn't cause double-navigation on first launch.

#### [MODIFY] [MainActivity2.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/ui/activities/MainActivity2.java)
- Remove redundant duplicate imports.

### 2. UI & Resource Safety
#### [MODIFY] [fragment_home.xml](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/res/layout/fragment_home.xml)
- Remove the invalid `style="bold"` attribute from the root `ConstraintLayout`.

#### [MODIFY] [AddDocumentFragment.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/ui/fragments/AddDocumentFragment.java)
- Replace `android.app.AlertDialog` with `androidx.appcompat.app.AlertDialog` for better theme support and stability.
- Add safety checks to ensure `getContext()` is not null before showing Toasts or Dialogs.

#### [MODIFY] [AddItemFragment.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/ui/fragments/AddItemFragment.java)
- Add safety checks (`isAdded()` and `getContext() != null`) before showing Toasts during product lookup callbacks.

### 3. Preferences Safety
#### [MODIFY] [PreferenceManager.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/data/PreferenceManager.java)
- Ensure all editor operations are lifecycle-aware where possible, or at least guarded against null context.

## Verification Plan

### Automated Tests
- Run `gradlew assembleDebug` to ensure no regression in build stability.
- Monitor Logcat for `IllegalStateException` or `NullPointerException` during navigation.

### Manual Verification
- **Splash Screen to Onboarding**: Verify the app transitions correctly from Splash to Onboarding without "stopping".
- **Onboarding to Main**: Complete registration and ensure `MainActivity` loads with the `HomeFragment`.
- **Todo Navigation**: Click "Add" and "Edit" in the Tasks screen to verify `Navigation Component` is handling the transitions.
- **Product Lookup**: Scan a barcode (if possible) or trigger lookup to verify no crashes occur when Toasts are shown.
