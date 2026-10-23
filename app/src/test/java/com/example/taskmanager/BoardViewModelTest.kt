package com.example.taskmanager

import androidx.lifecycle.SavedStateHandle
import com.example.taskmanager.core.data.TaskRepository
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.database.entity.TeamMemberEntity
import com.example.taskmanager.core.database.relation.MemberWithUser
import com.example.taskmanager.core.database.relation.ProjectWithCount
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.model.TeamRole
import com.example.taskmanager.feature.board.BoardViewModel
import com.example.taskmanager.util.FakeTaskRepository
import com.example.taskmanager.util.MainDispatcherRule
import com.example.taskmanager.util.StubTeamRepository
import com.example.taskmanager.util.testUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import app.cash.turbine.test

@OptIn(ExperimentalCoroutinesApi::class)
class BoardViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val userAn = testUser("u-an", "Nguyễn An")
    private val userBinh = testUser("u-binh", "Trần Bình")

    private val taskTodo = TaskWithAssignee(
        task = TaskEntity(
            id = "t-1",
            projectId = "p-1",
            title = "Task cần làm",
            status = TaskStatus.TODO,
            priority = Priority.HIGH,
            assigneeId = userAn.id
        ),
        assignee = userAn
    )

    private val taskDoing = TaskWithAssignee(
        task = TaskEntity(
            id = "t-2",
            projectId = "p-1",
            title = "Task đang làm",
            status = TaskStatus.DOING,
            priority = Priority.MEDIUM,
            assigneeId = userBinh.id
        ),
        assignee = userBinh
    )

    private val taskDone = TaskWithAssignee(
        task = TaskEntity(
            id = "t-3",
            projectId = "p-1",
            title = "Task đã xong",
            status = TaskStatus.DONE,
            priority = Priority.LOW,
            assigneeId = userAn.id
        ),
        assignee = userAn
    )

    private fun createTaskRepository(): FakeTaskRepository {
        return FakeTaskRepository(
            initialTasks = listOf(
                taskTodo,
                taskDoing,
                taskDone
            )
        )
    }

    private fun createTeamRepository(): StubTeamRepository {
        return StubTeamRepository(
            projects = listOf(
                ProjectWithCount(
                    "p-1",
                    "Dự án Android",
                    3
                )
            ),
            members = listOf(
                MemberWithUser(
                    TeamMemberEntity(
                        "t-1",
                        userAn.id,
                        TeamRole.OWNER
                    ),
                    userAn
                ),
                MemberWithUser(
                    TeamMemberEntity(
                        "t-1",
                        userBinh.id,
                        TeamRole.MEMBER
                    ),
                    userBinh
                )
            )
        )
    }

    private fun TestScope.createVm(
        projectId: String = "p-1",
        teamId: String = "t-1",
        repo: TaskRepository = createTaskRepository()
    ): BoardViewModel {

        /*
         * BoardViewModel sử dụng:
         *
         * savedState.toRoute<Board>().projectId
         * savedState.toRoute<Board>().teamId
         *
         * Vì vậy SavedStateHandle phải chứa đúng 2 key này.
         */
        val savedState = SavedStateHandle().apply {
            set("projectId", projectId)
            set("teamId", teamId)
        }

        val vm = BoardViewModel(
            savedState = savedState,
            repo = repo,
            repoTeam = createTeamRepository()
        )

        /*
         * Kích hoạt StateFlow để WhileSubscribed bắt đầu collect.
         */
        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            vm.tasks.collect {}
        }

        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            vm.members.collect {}
        }

        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            vm.projectName.collect {}
        }

        return vm
    }

    @Test
    fun initialState_hasTodoStatus_andLoadsProjectNameAndMembers() =
        runTest {

            val vm = createVm()

            assertEquals(
                TaskStatus.TODO,
                vm.selectedStatus.value
            )

            assertEquals(
                "Dự án Android",
                vm.projectName.value
            )

            assertEquals(
                2,
                vm.members.value.size
            )

            val tasks = vm.tasks.value

            assertEquals(
                1,
                tasks.size
            )

            assertEquals(
                "t-1",
                tasks.first().task.id
            )

            assertEquals(
                "Task cần làm",
                tasks.first().task.title
            )
        }

    @Test
    fun selectStatus_changesToDoing_andLoadsMatchingTasks() =
        runTest {

            val vm = createVm()

            vm.selectStatus(TaskStatus.DOING)

            assertEquals(
                TaskStatus.DOING,
                vm.selectedStatus.value
            )

            val tasks = vm.tasks.value

            assertEquals(
                1,
                tasks.size
            )

            assertEquals(
                "t-2",
                tasks.first().task.id
            )

            assertEquals(
                "Task đang làm",
                tasks.first().task.title
            )
        }

    @Test
    fun selectStatus_changesToDone_andLoadsMatchingTasks() =
        runTest {

            val vm = createVm()

            vm.selectStatus(TaskStatus.DONE)

            assertEquals(
                TaskStatus.DONE,
                vm.selectedStatus.value
            )

            val tasks = vm.tasks.value

            assertEquals(
                1,
                tasks.size
            )

            assertEquals(
                "t-3",
                tasks.first().task.id
            )

            assertEquals(
                "Task đã xong",
                tasks.first().task.title
            )
        }

    @Test
    fun createTask_callsRepositoryWithCorrectData() =
        runTest {

            val repo = createTaskRepository()

            val vm = createVm(
                repo = repo
            )

            vm.createTask(
                title = "Task kiểm thử tự động",
                description = "Mô tả chi tiết",
                priority = Priority.HIGH,
                dueDate = 1700000000000L,
                assigneeId = userAn.id
            )

            /*
             * createTask() chạy trong viewModelScope.
             *
             * Chờ coroutine hoàn thành trước khi assert.
             */
            testScheduler.advanceUntilIdle()

            assertEquals(
                1,
                repo.createdCalls.size
            )

            val createdTask = repo.createdCalls.first()

            assertEquals(
                "Task kiểm thử tự động",
                createdTask.title
            )

            assertEquals(
                "Mô tả chi tiết",
                createdTask.description
            )

            assertEquals(
                Priority.HIGH,
                createdTask.priority
            )

            assertEquals(
                "p-1",
                createdTask.projectId
            )

            assertEquals(
                userAn.id,
                createdTask.assigneeId
            )
        }

    @Test
    fun tasks_empty_whenNoTasksForSelectedStatus() =
        runTest {

            val emptyRepo = FakeTaskRepository(
                initialTasks = emptyList()
            )

            val vm = createVm(
                repo = emptyRepo
            )

            assertTrue(
                vm.tasks.value.isEmpty()
            )
        }

    @Test
    fun projectName_fallsBackToDefault_whenProjectNotFound() =
        runTest {

            val vm = createVm(
                projectId = "p-999"
            )

            assertEquals(
                "Bảng công việc",
                vm.projectName.value
            )
        }

    @Test
    fun createTask_success_sendsMessage() = runTest {
        val vm = createVm()
        vm.messages.test {
            vm.createTask("Task mới", null, Priority.LOW, null, null)
            assertEquals("Đã tạo công việc", awaitItem())
        }
    }

    @Test
    fun createTask_failure_sendsErrorMessage() = runTest {
        val repo = createTaskRepository().apply { createError = IllegalStateException("Lỗi lưu") }
        val vm = createVm(repo = repo)
        vm.messages.test {
            vm.createTask("Task mới", null, Priority.LOW, null, null)
            assertEquals("Lỗi lưu", awaitItem())
        }
        assertEquals(0, repo.createdCalls.size)
    }
}
