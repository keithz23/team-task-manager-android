package com.example.taskmanager

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.core.data.TaskRepository
import com.example.taskmanager.core.database.SeedData
import com.example.taskmanager.core.database.dao.TaskDao
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.session.SessionManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TaskRepositoryTest {

    @get:Rule val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var repo: TaskRepository
    @Inject lateinit var session: SessionManager
    @Inject lateinit var taskDao: TaskDao

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    // 1. Kiểm tra đọc chi tiết một task cùng người được giao
    @Test
    fun observe_returnsTaskWithAssignee() = runTest {
        val item = repo.observe(SeedData.TASK_TODO).first()

        assertNotNull(item)
        assertEquals("Thiết kế màn hình đăng nhập", item?.task?.title)
        assertEquals(SeedData.USER_AN, item?.assignee?.id)
        assertEquals("Nguyễn An", item?.assignee?.name)
    }

    // 2. Kiểm tra lọc task theo trạng thái (TODO, DOING, DONE)
    @Test
    fun observeByStatus_filtersCorrectly() = runTest {
        val todoTasks = repo.observeByStatus(SeedData.PROJECT_ID, TaskStatus.TODO).first()
        assertEquals(1, todoTasks.size)
        assertEquals(SeedData.TASK_TODO, todoTasks.first().task.id)

        val doingTasks = repo.observeByStatus(SeedData.PROJECT_ID, TaskStatus.DOING).first()
        assertEquals(1, doingTasks.size)
        assertEquals(SeedData.TASK_DOING, doingTasks.first().task.id)

        val doneTasks = repo.observeByStatus(SeedData.PROJECT_ID, TaskStatus.DONE).first()
        assertEquals(1, doneTasks.size)
        assertEquals(SeedData.TASK_DONE, doneTasks.first().task.id)
    }

    // 3. Kiểm tra đếm số lượng task theo từng trạng thái
    @Test
    fun observeCounts_returnsCountsForEachStatus() = runTest {
        val counts = repo.observeCounts(SeedData.PROJECT_ID).first()
        val countMap = counts.associate { it.status to it.count }

        assertEquals(1, countMap[TaskStatus.TODO])
        assertEquals(1, countMap[TaskStatus.DOING])
        assertEquals(1, countMap[TaskStatus.DONE])
    }

    // 4. Kiểm tra lấy danh sách việc của tôi (loại bỏ task đã DONE)
    @Test
    fun observeMine_returnsOnlyMyTasksExcludingDone() = runTest {
        session.save(SeedData.USER_AN)

        val myTasks = repo.observeMine().first()
        assertEquals(1, myTasks.size)
        assertEquals(SeedData.TASK_TODO, myTasks.first().task.id)
    }

    // 5. Kiểm tra tạo task mới thành công
    @Test
    fun create_validTask_insertsSuccessfully() = runTest {
        val result = repo.create(
            projectId = SeedData.PROJECT_ID,
            title = "Viết test tự động",
            description = "Mô tả chi tiết",
            priority = Priority.HIGH,
            dueDate = 1750000000000L,
            assigneeId = SeedData.USER_BINH
        )

        assertTrue(result.isSuccess)
        val todoTasks = repo.observeByStatus(SeedData.PROJECT_ID, TaskStatus.TODO).first()
        assertTrue(todoTasks.any { it.task.title == "Viết test tự động" })
    }

    // 6. Kiểm tra validate: Tiêu đề trống phải trả về lỗi
    @Test
    fun create_blankTitle_returnsFailure() = runTest {
        val result = repo.create(
            projectId = SeedData.PROJECT_ID,
            title = "   ",
            description = null,
            priority = Priority.MEDIUM,
            dueDate = null,
            assigneeId = null
        )

        assertTrue(result.isFailure)
        assertEquals("Tiêu đề của dự án không được phép trống!", result.exceptionOrNull()?.message)
    }

    // 7. Kiểm tra cập nhật trạng thái
    @Test
    fun updateStatus_changesStatusSuccessfully() = runTest {
        repo.updateStatus(SeedData.TASK_TODO, TaskStatus.DONE)

        val item = repo.observe(SeedData.TASK_TODO).first()
        assertEquals(TaskStatus.DONE, item?.task?.status)
    }

    // 8. Kiểm tra phân công task cho người khác
    @Test
    fun assign_changesAssigneeSuccessfully() = runTest {
        repo.assign(SeedData.TASK_TODO, SeedData.USER_BINH)

        val item = repo.observe(SeedData.TASK_TODO).first()
        assertEquals(SeedData.USER_BINH, item?.task?.assigneeId)
        assertEquals(SeedData.USER_BINH, item?.assignee?.id)
    }

    // 9. Kiểm tra xóa task
    @Test
    fun delete_removesTaskFromDatabase() = runTest {
        repo.delete(SeedData.TASK_TODO)

        val item = repo.observe(SeedData.TASK_TODO).first()
        assertNull(item)
    }
}