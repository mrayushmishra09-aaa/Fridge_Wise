# Barcode Intelligence Implementation Plan

This plan introduces a high-impact Barcode Scanning feature to FridgeWise, enabling users to add food items by scanning their barcodes, which automatically fetches product details (name, category, brand) via the OpenFoodFacts API.

## User Review Required

> [!IMPORTANT]
> This feature requires **Camera** and **Internet** permissions. Users will see a permission prompt the first time they try to scan.

## Proposed Changes

### [Core Dependencies]

#### [MODIFY] [libs.versions.toml](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/gradle/libs.versions.toml)
- Add versions and library definitions for Google ML Kit (Barcode Scanning), CameraX, and OkHttp.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/build.gradle.kts)
- Implement the newly added libraries.

---

### [Infrastructure & Permissions]

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/AndroidManifest.xml)
- Add `<uses-permission android:name="android.permission.CAMERA" />`.
- Add `<uses-permission android:name="android.permission.INTERNET" />`.

---

### [Barcode Scanning UI & Logic]

#### [NEW] [BarcodeScannerActivity.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/BarcodeScannerActivity.java)
- A dedicated Activity using CameraX to provide a smooth scanning experience.
- Uses ML Kit to detect barcodes and returns the result to the calling fragment.

#### [NEW] [activity_barcode_scanner.xml](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/res/layout/activity_barcode_scanner.xml)
- Layout for the scanner, including the camera preview and a scanning "reticle" (visual guide).

---

### [Product Lookup & Integration]

#### [NEW] [ProductLookupManager.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/ProductLookupManager.java)
- Handles network calls to the OpenFoodFacts API.
- Parses the JSON response into a simple `Product` object.

#### [MODIFY] [AddItemFragment.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/AddItemFragment.java)
- Add a "Scan Barcode" button next to the item name field.
- Launch the scanner and handle the returned barcode.
- Use `ProductLookupManager` to auto-fill the form fields.

## Verification Plan

### Automated Tests
- N/A (Focus on manual verification for hardware/API integration).

### Manual Verification
1.  Open **Add Item** screen.
2.  Tap the **Barcode Scanner** icon.
3.  Grant camera permissions.
4.  Scan a common grocery item (e.g., a Coke can or a bag of chips).
5.  Verify the **Item Name** and **Category** are automatically populated.
6.  Save the item and ensure it appears in the inventory correctly.
