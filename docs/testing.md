# FridgeWise Testing Strategy

This document outlines the testing infrastructure and patterns for the FridgeWise application.

## Test Infrastructure

We use a modern Android testing stack:
- **JUnit 4:** The standard test runner.
- **Mockito (Java):** For mocking dependencies in unit tests.
- **Mockito-inline:** For mocking static methods like `AppDatabase.getInstance()`.
- **Robolectric:** For running local unit tests that depend on the Android framework (like `Context`, `AlarmManager`, `Permissions`).
- **Room Test:** For verifying database integrity.
- **WorkManager Test:** For verifying background operations in workers.

## Types of Tests

### 1. Local Unit Tests (`app/src/test`)
These tests run on your development machine (JVM). Use them for business logic, utilities, and ViewModel logic.
- **ReminderCoordinatorTest:** Verifies alarm scheduling.
- **NotificationPersonalityEngineTest:** Verifies humanized message logic.
- **PermissionManagerTest:** Verifies permission health check logic.

### 2. Instrumented Integration Tests (`app/src/androidTest`)
These tests run on an Android device or emulator. Use them for database (Room) and background tasks (WorkManager).
- **MedicineDaoTest:** Verifies CRUD operations on the medicine table.
- **NotificationActionWorkerTest:** Verifies that clicking "Take Dose" correctly updates the database.

## Running Tests

### Using Android Studio
- **Run all tests:** Right-click on the `java` folder in `app/src/test` and select "Run 'Tests in...'".
- **Run instrumented tests:** Right-click on the `java` folder in `app/src/androidTest` and select "Run 'Tests in...'".

### Using Command Line
```bash
# Run local unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedCheck
```

## Best Practices
- **Mock Statics:** Use `MockedStatic` from Mockito-inline to mock static dependencies like `AppDatabase.getInstance()`.
- **In-Memory Databases:** Always use `Room.inMemoryDatabaseBuilder()` for DAO tests to ensure tests are isolated and don't affect real data.
- **Worker Testing:** Use `TestWorkerBuilder` to test workers in isolation.
- **Dependency Injection:** Classes with side effects (like `ReminderCoordinator` using `Executor`) should allow injecting dependencies in the constructor for easier testing.
