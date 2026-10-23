package com.example.taskmanager

import androidx.lifecycle.SavedStateHandle
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.database.entity.TeamMemberEntity
import com.example.taskmanager.core.database.relation.MemberWithUser
import com.example.taskmanager.core.database.relation.ProjectWithCount
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.database.relation.TeamWithCounts
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.model.TeamRole
import com.example.taskmanager.feature.taskdetail.TaskDetailViewModel
import com.example.taskmanager.util.FakeSessionManager
import com.example.taskmanager.util.FakeTaskRepository
import com.example.taskmanager.util.MainDispatcherRule
import com.example.taskmanager.util.StubTeamRepository
import com.example.taskmanager.util.testUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    // 1. Tạo 2 user: An (người làm task) và Bình (người ngoài)
    private val userAn = testUser("u-an", "Nguyễn An")
    private val userBinh = testUser("u-binh", "Trần Bình")

    // 2. Task mẫu được giao cho user An
    private val sampleTask = TaskWithAssignee(
        task = TaskEntity(
            id = "t-1",
            projectId = "p-1",
            title = "Task kiểm thử chi tiết",
            description = "Mô tả ban đầu",
            status = TaskStatus.TODO,
            priority = Priority.MEDIUM,
            dueDate = 1700000000000L,
            assigneeId = userAn.id
        ),
        assignee = userAn
    )

    /**
     * Hàm helper khởi tạo ViewModel với quyền (Role) và User đăng nhập tùy chỉnh
     */
    private fun TestScope.createVm(
        currentUserId: String = userAn.id,
        userRole: TeamRole = TeamRole.MEMBER,
        taskId: String = "t-1",
        taskRepo: FakeTaskRepository = FakeTaskRepository(listOf(sampleTask))
    ): Pair<TaskDetailViewModel, FakeTaskRepository> {
        val savedState = SavedStateHandle(mapOf("taskId" to taskId))
        val session = FakeSessionManager(initialUserId = currentUserId)

        val teamRepo = StubTeamRepository(
            projects = listOf(ProjectWithCount("p-1", "Dự án Android", 1)),
            teams = listOf(TeamWithCounts("t-1", "Nhóm Android", userRole, 2)),
            members = listOf(
                MemberWithUser(TeamMemberEntity("t-1", userAn.id, userRole), userAn),
                MemberWithUser(TeamMemberEntity("t-1", userBinh.id, TeamRole.MEMBER), userBinh)
            )
        )

        val vm = TaskDetailViewModel(savedState, taskRepo, teamRepo, session)

        // Kích hoạt các luồng StateFlow chạy ngầm
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.task.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.canEdit.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.canDelete.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.members.collect {} }

        return vm to taskRepo
    }

    // --- CÁC HÀM TEST ---

    @Test
    fun loadsTaskSuccessfully_whenInitialized() = runTest {
        val (vm, _) = createVm()

        val loadedTask = vm.task.value
        assertNotNull(loadedTask)
        assertEquals("t-1", loadedTask?.task?.id)
        assertEquals("Task kiểm thử chi tiết", loadedTask?.task?.title)
        assertEquals(userAn.id, loadedTask?.assignee?.id)
    }

    @Test
    fun owner_canEditAndCanDelete() = runTest {
        // Giả lập user đăng nhập có quyền OWNER
        val (vm, _) = createVm(currentUserId = userBinh.id, userRole = TeamRole.OWNER)

        // Trưởng nhóm được quyền cả sửa lẫn xóa
        assertTrue(vm.canEdit.value)
        assertTrue(vm.canDelete.value)
    }

    @Test
    fun assignee_canEdit_butCannotDelete() = runTest {
        // Giả lập An đăng nhập (là người được giao task nhưng chỉ là MEMBER)
        val (vm, _) = createVm(currentUserId = userAn.id, userRole = TeamRole.MEMBER)

        // Người làm task được sửa thông tin nhưng KHÔNG được xóa task
        assertTrue(vm.canEdit.value)
        assertFalse(vm.canDelete.value)
    }

    @Test
    fun otherMember_cannotEditAndCannotDelete() = runTest {
        // Giả lập Bình đăng nhập (không phải người làm task, cũng chỉ là MEMBER)
        val (vm, _) = createVm(currentUserId = userBinh.id, userRole = TeamRole.MEMBER)

        // Người ngoài chỉ được xem, KHÔNG được sửa và KHÔNG được xóa
        assertFalse(vm.canEdit.value)
        assertFalse(vm.canDelete.value)
    }

    @Test
    fun updateStatus_callsRepository() = runTest {
        val (vm, repo) = createVm()

        vm.updateStatus(TaskStatus.DOING)
        advanceUntilIdle()

        val updated = repo.tasks.value.first { it.task.id == "t-1" }
        assertEquals(TaskStatus.DOING, updated.task.status)
    }

    @Test
    fun updateTask_callsRepositoryWithNewData() = runTest {
        val (vm, repo) = createVm()
        vm.updateTask(
            title = "Tiêu đề đã cập nhật",
            description = "Mô tả mới",
            priority = Priority.HIGH,
            dueDate = 1800000000000L,
            assigneeId = userBinh.id
        )
        advanceUntilIdle()

        // Assert: Thông tin task trong Repo được cập nhật chuẩn xác
        val updated = repo.tasks.value.first { it.task.id == "t-1" }
        assertEquals("Tiêu đề đã cập nhật", updated.task.title)
        assertEquals("Mô tả mới", updated.task.description)
        assertEquals(Priority.HIGH, updated.task.priority)
        assertEquals(userBinh.id, updated.task.assigneeId)
    }
}