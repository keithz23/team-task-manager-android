
# Team Task Manager (Android)

A Jira-style team task management application built for Android as a final project for a Mobile Development course, developed using **Kotlin** and **Jetpack Compose**.

## Features
* **Authentication:** Secure sign-up and sign-in with local accounts and salted password hashing.
* **Team & Project Management:** Create teams, add members, and manage collaborative projects.
* **Kanban Board:** Interactive board featuring columns for **To-Do**, **Doing**, and **Done**.
* **Task Management:** Create and edit tasks with adjustable priorities, due dates, and assignees.
* **Collaboration & Personal View:** Add comments to tasks and view a dedicated "My Tasks" dashboard.

## Tech Stack
* **Language & Concurrency:** Kotlin, Coroutines, Flow / StateFlow
* **UI Framework:** Jetpack Compose + Material 3
* **Local Persistence:** Room Database utilizing the Repository pattern
* **Dependency Injection:** Hilt
* **Navigation:** Type-safe Navigation Compose
* **Testing & CI/CD:** JUnit, Compose UI Tests, GitHub Actions CI

## Folder Structure

```text
app/src/main/java/com/example/taskmanager/
├── TaskApp.kt, MainActivity.kt
├── core/
│   ├── model/         Enums and shared data models
│   ├── database/      AppDatabase, entity/, dao/, relation/
│   ├── session/       SessionStore (DataStore), SessionViewModel
│   ├── security/      PasswordHasher
│   ├── di/            DatabaseModule, RepositoryModule
│   ├── ui/            theme/, component/
│   └── util/          Utility extensions and helpers
├── feature/           auth, team, board, taskdetail, mytasks, profile
│                      (Each feature contains: Repository + ViewModel + Screen)
└── navigation/        Routes, AppNavHost
app/src/test/          JVM unit tests
app/src/androidTest/   Room in-memory & Compose UI tests
.github/workflows/     android-ci.yml
```

## Running the Project

```bash
./gradlew testDebugUnitTest             # Run unit tests
./gradlew assembleDebug                 # Build debug APK
./gradlew connectedDebugAndroidTest     # Run UI tests (requires emulator or connected device)
