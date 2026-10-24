package com.example.taskmanager.feature.mytasks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.ui.Tags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MyTasksScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun myTasksContent_showEmptyState_whenTaskListIsEmpty() {
        // Arrange & Act
        composeTestRule.setContent {
            MyTasksContent(
                tasks = emptyList(),
                onTask = {}
            )
        }

        // Assert
        composeTestRule
            .onNodeWithText("Việc của tôi")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("0 task đang mở")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Không có công việc nào")
            .assertIsDisplayed()
    }

    @Test
    fun myTasksContent_showTaskList_whenTasksAreAvailable() {
        // Arrange
        val mockTasks = listOf(
            TaskWithAssignee(
                task = TaskEntity(
                    id = "task-1",
                    projectId = "proj-1",
                    title = "Viết Unit Test cho MyTasksScreen",
                    description = "Mô tả task test",
                    status = TaskStatus.TODO,
                    priority = Priority.HIGH,
                    createdAt = System.currentTimeMillis()
                ),
                assignee = null
            ),
            TaskWithAssignee(
                task = TaskEntity(
                    id = "task-2",
                    projectId = "proj-1",
                    title = "Fix lỗi UI padding",
                    description = "Mô tả task test",
                    status = TaskStatus.DOING,
                    priority = Priority.MEDIUM,
                    createdAt = System.currentTimeMillis()
                ),
                assignee = null
            )
        )

        // Act
        composeTestRule.setContent {
            MyTasksContent(
                tasks = mockTasks,
                onTask = {}
            )
        }

        // Assert
        composeTestRule
            .onNodeWithText("2 task đang mở")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(Tags.TASK_LIST)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Viết Unit Test cho MyTasksScreen")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Fix lỗi UI padding")
            .assertIsDisplayed()

        // Mỗi thẻ hiện nhãn trạng thái để phân biệt việc chưa làm và đang làm
        composeTestRule.onNodeWithText("Cần làm").assertIsDisplayed()
        composeTestRule.onNodeWithText("Đang làm").assertIsDisplayed()
    }

    @Test
    fun myTasksContent_clickTaskItem_triggersOnTaskCallback() {
        // Arrange
        var clickedTaskId: String? = null
        val mockTask = TaskWithAssignee(
            task = TaskEntity(
                id = "task-123",
                projectId = "proj-1",
                title = "Thử nghiệm Click Task",
                description = "",
                status = TaskStatus.TODO,
                priority = Priority.LOW,
                createdAt = System.currentTimeMillis()
            ),
            assignee = null
        )

        composeTestRule.setContent {
            MyTasksContent(
                tasks = listOf(mockTask),
                onTask = { taskId -> clickedTaskId = taskId }
            )
        }

        // Act
        composeTestRule
            .onNodeWithText("Thử nghiệm Click Task")
            .performClick()

        // Assert
        assertEquals("task-123", clickedTaskId)
    }
}