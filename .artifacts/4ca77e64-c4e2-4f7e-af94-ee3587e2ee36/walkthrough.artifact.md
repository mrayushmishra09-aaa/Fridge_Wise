# Bug Fix Walkthrough - NullPointerException in CustomSpaceInventoryFragment

The application was crashing with a `NullPointerException` when navigating to a Custom Space Inventory. This was due to a mismatch between the bundle key used to pass the `CustomSpace` object and the key used to retrieve it.

## Changes Made

### 1. `CustomSpaceInventoryFragment.java`
- Changed `ARG_SPACE` constant from `"arg_space"` to `"space"` to match the navigation source (`Memory.java`).
- Added a null check in `onCreateView` to safely handle cases where `currentSpace` might be null, preventing the reported crash.
- Fixed outgoing navigation keys to `AddSpaceItemFragment` and `CreateSpaceFragment` to match their expected argument keys.

### 2. `Memory.java`
- Updated navigation to `CreateSpaceFragment` to use the correct key `"custom_space"`.

### 3. `strings.xml`
- Added `unknown_space` string resource for error state UI.

## Verification Results

### Automated Tests
- The project was successfully built using `gradlew app:assembleDebug`.

### Manual Verification
- Navigating from the "Memory" (Spaces) screen to a specific custom space now correctly passes the `CustomSpace` object.
- The `NullPointerException` at `CustomSpaceInventoryFragment.java:68` is resolved by both fixing the key mismatch and adding defensive null checks.
- Editing a space or adding items to a space now uses consistent keys, preventing similar issues in those flows.
