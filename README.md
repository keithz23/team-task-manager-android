# Team Task Manager (Android)

[![Android CI](https://github.com/keithz23/team-task-manager-android/actions/workflows/android-ci.yml/badge.svg?branch=develop)](https://github.com/keithz23/team-task-manager-android/actions/workflows/android-ci.yml)

A Jira-style team task management application built for Android as a final project for a Mobile Development course, developed using **Kotlin** and **Jetpack Compose**. Local-only: all data is stored on the device with Room.

## Features
* **Authentication:** Secure sign-up and sign-in with local accounts and salted password hashing (PBKDF2).
* **Team & Project Management:** Create teams, add members, and manage collaborative projects.
* **Kanban Board:** Interactive board featuring columns for **To-Do**, **Doing**, and **Done**.
* **Task Management:** Create and edit tasks with adjustable priorities, due dates, and assignees.
* **Collaboration & Personal View:** Add comments to tasks and view a dedicated "My Tasks" dashboard.

## Tech Stack
* **Language & Concurrency:** Kotlin, Coroutines, Flow / StateFlow
* **UI Framework:** Jetpack Compose + Material 3
* **Architecture:** MVVM (unidirectional data flow), Repository pattern
* **Local Persistence:** Room, DataStore (session)
* **Dependency Injection:** Hilt
* **Navigation:** Type-safe Navigation Compose
* **Testing:** JUnit 4, Turbine, kotlinx-coroutines-test, Room in-memory, Hilt testing, Compose UI tests
* **CI:** GitHub Actions (unit tests + UI tests on an emulator)

## Getting Started

Requirements: Android Studio (Koala or newer) with **JDK 17**, an emulator or device running Android 8.0 (API 26)+.

```bash
git clone https://github.com/keithz23/team-task-manager-android.git
cd team-task-manager-android
git checkout develop
./gradlew assembleDebug        # build the debug APK
```

Or open the project in Android Studio and press **Run**.

### Demo accounts (debug build)
The debug build seeds demo data on first launch (`DebugSeeder` → `SeedData`):

| Email | Password | Role |
|---|---|---|
| `an@demo.com` | `123456` | Owner of "Team Demo" |
| `binh@demo.com` | `123456` | Member of "Team Demo" |

The team has one project with 3 tasks (To-Do / Doing / Done); the To-Do task already has 2 comments.
To start from an empty database, uninstall the app (or clear its storage) — the seed runs again on the next launch.

## Project Structure

```text
app/src/main/java/com/example/taskmanager/
├── TaskApp.kt, MainActivity.kt
├── core/
│   ├── data/          Repository interfaces + implementations (Task, Team, Comment)
│   ├── database/      AppDatabase, entity/, dao/, relation/, SeedData, DebugSeeder
│   ├── di/            DbModule, RepositoryModule
│   ├── model/         Enums (TaskStatus, Priority, TeamRole)
│   ├── security/      PasswordHasher
│   ├── session/       SessionManager (DataStore), SessionModule
│   ├── ui/            theme/, component/ (TaskCard, PriorityChip, AvatarInitials, …), Tags
│   └── util/          Extensions (DateExt, …)
├── feature/           auth, team, board, taskdetail, comment, mytasks, profile
│                      (each feature: Screen + ViewModel, navigation/ if it owns routes)
└── navigation/        Routes, AppNavHost, graphs
app/src/debug/         HiltTestActivity (used by UI tests only)
app/src/test/          JVM unit tests
app/src/androidTest/   Room in-memory tests & Compose UI tests
.github/workflows/     android-ci.yml
```

Coding rules, shared interfaces and task split: see [`dev_guide.md`](dev_guide.md).

## Testing

```bash
./gradlew testDebugUnitTest             # unit tests (JVM, fast)
./gradlew connectedDebugAndroidTest     # Room + UI tests (needs a running emulator/device)
```

Reports: `app/build/reports/tests/testDebugUnitTest/index.html` and `app/build/reports/androidTests/connected/index.html`.

### Shared test infrastructure
| What | Where | Use it for |
|---|---|---|
| `MainDispatcherRule` | `test/.../util/` | Every ViewModel unit test: `@get:Rule val mainRule = MainDispatcherRule()` |
| `FakeSessionManager` | `test/.../util/` | Fake login in unit tests: `FakeSessionManager("user-id")` |
| Fake repositories | `test/.../util/Fakes.kt` | In-memory repositories for ViewModel tests (add yours here) |
| `TestDbModule`, `TestSessionModule` | `androidTest/.../di/TestModules.kt` | Automatically replace the real DB/session in every `@HiltAndroidTest`: fresh in-memory Room DB with `SeedData`, fake session (logged out) |
| `FakeSessionManager` | `androidTest/.../util/` | `@Inject lateinit var session: SessionManager` then `session.save(SeedData.USER_AN)` to log in |
| `HiltTestActivity` | `debug/` | Render a single screen with Hilt: `createAndroidComposeRule<HiltTestActivity>()` |
| `SeedData` | `main/.../core/database/` | Fixed IDs for test data (`USER_AN`, `TEAM_ID`, `PROJECT_ID`, `TASK_TODO`, …) |
| `Tags` | `main/.../core/ui/Tags.kt` | `Modifier.testTag(Tags.X)` on elements a UI test needs to find |

Minimal UI test:

```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MyScreenTest {
    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<HiltTestActivity>()
    @Inject lateinit var session: SessionManager

    @Before fun setUp() {
        hiltRule.inject()
        runBlocking { session.save(SeedData.USER_AN) }
        composeRule.setContent { AppTheme { MyScreen() } }
    }
}
```

## Continuous Integration

Workflow: [`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml). Runs on every pull request and on pushes to `main` / `develop`.

| Job | What it runs | Report artifact |
|---|---|---|
| `unit-test` | `./gradlew testDebugUnitTest assembleDebug` | `unit-test-report` |
| `ui-test` | `./gradlew connectedDebugAndroidTest` on an API 34 emulator (AVD cached between runs) | `ui-test-report` |

A new push to the same branch cancels the previous run. Download reports from the run page → **Artifacts**.

## Contributing

```
main      ← release only: merged from develop, tagged for submission
develop   ← integration branch: protected, merge via PR only
feat/<name>-<topic>   e.g. feat/phuong-comment, feat/thu-team
```

1. Branch from the latest `develop`: `git checkout develop && git pull && git checkout -b feat/<name>-<topic>`.
2. Commit with a type prefix: `feat(task): …`, `fix(auth): …`, `test(team): …`, `chore: …`, `docs: …`.
3. Write unit tests + a UI test for your screen (part of the Definition of Done).
4. Open a PR into **`develop`**. It can be merged only when **`unit-test` and `ui-test` are green** and it has **1 approval**.
