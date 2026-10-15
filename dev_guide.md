# Task Manager (Android) - Hướng dẫn code & phân công

Ứng dụng quản lý công việc nhóm, bản **local-only** (Room), thời hạn ~2 tuần.
Yêu cầu bắt buộc: Room + Repository pattern, Unit test + UI test, CI GitHub Actions chạy test khi push.

## 1. Tech stack

Kotlin, Jetpack Compose + Material 3, MVVM (UDF), Hilt, Room, DataStore, Navigation Compose (type-safe), Coroutines + Flow.
Test: JUnit4, MockK, Turbine, coroutines-test, Room in-memory, Hilt test, Compose UI test.

## 2. Cấu trúc package

```
com.example.taskmanager/
├── TaskApp.kt, MainActivity.kt
├── core/
│   ├── database/        # AppDatabase, entity, DAO, converters
│   ├── session/         # SessionManager (DataStore)
│   ├── di/              # DbModule, SessionModule
│   ├── ui/              # theme, component dùng chung, Tags
│   └── util/            # Result, extensions, PasswordHasher
├── feature/
│   ├── auth/            # data/ (AuthRepositoryImpl), presentation/, navigation/, di/
│   ├── team/
│   └── task/            # gồm cả comment
└── navigation/          # Routes.kt, AppNavHost.kt
```

Mỗi feature: `data/` (RepositoryImpl) · `presentation/` (Screen + ViewModel + UiState) · `navigation/` (XxxGraph.kt) · `di/` (@Binds).

## 3. Quy tắc code

1. **UI → ViewModel → Repository (interface) → DAO.** Screen không gọi DAO. ViewModel chỉ biết interface.
2. State là một `data class XxxUiState` trong `StateFlow`; sự kiện người dùng là hàm của ViewModel.
3. Screen chia 2 composable: `XxxScreen(viewModel)` (lấy state) và `XxxContent(state, onXxx)` (thuần UI, dễ preview/test).
4. Lấy user hiện tại bằng `SessionManager.currentUserId()`. Không truyền `userId` qua route.
5. Mọi phần tử UI test cần đều gắn `Modifier.testTag(Tags.X)`; thêm tag vào `core/ui/Tags.kt`.
6. Lỗi nghiệp vụ trả `Result<T>`; ViewModel map sang `error: String?` trong UiState.
7. Không hardcode màu/size, dùng `MaterialTheme` + component chung.
8. Mật khẩu: PBKDF2WithHmacSHA256 + salt ngẫu nhiên, **không** lưu plaintext.

### Mẫu ViewModel

```kotlin
data class BoardUiState(
    val selected: TaskStatus = TaskStatus.TODO,
    val tasks: List<TaskWithAssignee> = emptyList(),
    val counts: Map<TaskStatus, Int> = emptyMap(),
    val error: String? = null,
)

@HiltViewModel
class BoardViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repo: TaskRepository,
) : ViewModel() {
    private val projectId = savedState.toRoute<Board>().projectId
    private val selected = MutableStateFlow(TaskStatus.TODO)

    val state: StateFlow<BoardUiState> = combine(
        selected,
        selected.flatMapLatest { repo.observeByStatus(projectId, it) },
        repo.observeCounts(projectId),
    ) { s, tasks, counts ->
        BoardUiState(s, tasks, counts.associate { it.status to it.count })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BoardUiState())

    fun select(s: TaskStatus) { selected.value = s }
    fun move(id: String, to: TaskStatus) = viewModelScope.launch { repo.updateStatus(id, to) }
}
```

## 4. Hợp đồng chung (không tự ý đổi)

Đổi interface/route phải mở PR riêng và báo cả nhóm.

```kotlin
interface AuthRepository {
    suspend fun register(name: String, email: String, password: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun logout()
}
interface TeamRepository {
    fun observeTeams(): Flow<List<TeamSummary>>
    fun observeProjects(teamId: String): Flow<List<ProjectSummary>>
    fun observeMembers(teamId: String): Flow<List<MemberWithUser>>
    suspend fun createTeam(name: String, firstProject: String?): Result<String>
    suspend fun createProject(teamId: String, name: String): Result<Unit>
    suspend fun addMemberByEmail(teamId: String, email: String): Result<Unit>
}
interface TaskRepository {
    fun observeByStatus(projectId: String, status: TaskStatus): Flow<List<TaskWithAssignee>>
    fun observeCounts(projectId: String): Flow<List<StatusCount>>
    fun observeMine(): Flow<List<TaskWithAssignee>>
    fun observe(taskId: String): Flow<TaskWithAssignee?>
    suspend fun create(projectId: String, title: String, description: String?,
                       priority: Priority, dueDate: Long?, assigneeId: String?): Result<Unit>
    suspend fun update(task: TaskEntity): Result<Unit>
    suspend fun updateStatus(id: String, status: TaskStatus)
    suspend fun assign(id: String, userId: String?)
    suspend fun delete(id: String)
}
interface CommentRepository {
    fun observe(taskId: String): Flow<List<CommentWithAuthor>>
    suspend fun add(taskId: String, content: String): Result<Unit>
}
```

Routes (`navigation/Routes.kt`): `Login, Register, Teams, MyTasks, Profile, NewTeam, TeamDetail(teamId), Board(projectId, teamId), TaskDetail(taskId)`.
`AssigneePicker` là `ModalBottomSheet`/dialog trong Board, không cần route.

Quy tắc: mỗi người tự viết unit test + UI test cho phần của mình (thuộc Definition of Done của PR).


## 5. Quy trình Git

```
main      ← chỉ merge từ develop, tag bản nộp
develop   ← CI xanh + 1 review mới được merge
feat/<tên>-<việc>   vd: feat/dev2-board, feat/dev1-login
```

- 1 PR = 1 màn hình + test của nó, nhỏ để review nhanh. Review trong ngày.
- Commit theo dạng `feat(task): ...`, `fix(auth): ...`, `test(team): ...`.
- Chỉ **Dev 3** sửa `AppNavHost.kt`. Mỗi người chỉ thêm file trong `feat/<của mình>/`.
- Mỗi sáng 5 phút: hôm qua xong gì, hôm nay làm gì, đang bị chặn bởi ai.
- 
## 6. CI (`.github/workflows/android-ci.yml`)

```yaml
name: Android CI
on:
  push: { branches: [main, develop] }
  pull_request:
jobs:
  unit-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew assembleDebug testDebugUnitTest
      - uses: actions/upload-artifact@v4
        if: always()
        with: { name: unit-test-report, path: app/build/reports/tests/ }
  ui-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4
      - name: Enable KVM
        run: |
          echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
          sudo udevadm control --reload-rules && sudo udevadm trigger --name-match=kvm
      - uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 34
          arch: x86_64
          script: ./gradlew connectedDebugAndroidTest
```
