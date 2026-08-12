# Implementation Plan - Task Completion Visual Feedback

This plan outlines the changes required to provide visual feedback when a To-Do task is marked as completed. This includes adding a strikethrough to the title, dimming the item, and updating the database state.

## User Review Required

> [!IMPORTANT]
> The visual changes will include:
> 1. Strikethrough effect on the task title.
> 2. Faded text color for the title and time.
> 3. Reduced overall opacity for the task item.
> 4. Hiding the "Priority" and "Reminder" indicators for completed tasks to reduce clutter.

## Proposed Changes

### [Component Name] Todo UI & Logic

#### [MODIFY] [TodoAdapter.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/TodoAdapter.java)
- Add `onStatusChange(TodoItem item, boolean isCompleted)` to the `OnTodoItemClickListener` interface.
- Implement `CheckBox` click listener in `onBindViewHolder`.
- Create a helper method `applyCompletedStyle(TodoViewHolder holder, boolean isCompleted)` to handle visual updates:
    - Set/Remove `Paint.STRIKE_THRU_TEXT_FLAG` on the title.
    - Adjust text colors using `ContextCompat`.
    - Adjust `holder.itemView.setAlpha()`.
    - Toggle visibility of `tvPriority` and `ivNotification`.

#### [MODIFY] [TodoListFragment.java](file:///C:/Users/AYUSH/AndroidStudioProjects/FridgeWise/app/src/main/java/com/example/fridgewise/TodoListFragment.java)
- Implement the new `onStatusChange` callback in the fragment.
- Add a background thread operation to update the task's `isCompleted` status in the Room database.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors.

### Manual Verification
1. Launch the app.
2. Navigate to the To-Do list.
3. Tap the checkbox of an active task.
4. Verify the title gets a strikethrough and the item fades.
5. Restart the app to verify the state persists.
