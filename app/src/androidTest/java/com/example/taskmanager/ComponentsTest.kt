package com.example.taskmanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.database.entity.UserEntity
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.ui.component.AppTextField
import com.example.taskmanager.core.ui.component.PrimaryButton
import com.example.taskmanager.core.ui.component.PriorityChip
import com.example.taskmanager.core.ui.component.TaskCard
import com.example.taskmanager.core.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Kiểm tra các component dùng chung trong core/ui/component. */
@RunWith(AndroidJUnit4::class)
class ComponentsTest {

    @get:Rule val rule = createComposeRule()

    @Test
    fun appTextField_testTagReceivesTextInput() {
        rule.setContent {
            AppTheme {
                var text by remember { mutableStateOf("") }
                AppTextField(value = text, onValueChange = { text = it }, label = "Email", testTag = "field")
            }
        }
        rule.onNodeWithTag("field").performTextReplacement("an@demo.com")
        rule.onNodeWithTag("field").assertTextContains("an@demo.com")
    }

    @Test
    fun primaryButton_loadingDisablesButton() {
        var loading by mutableStateOf(false)
        var clicks = 0
        rule.setContent {
            AppTheme { PrimaryButton("Gửi", { clicks++ }, Modifier.testTag("btn"), loading = loading) }
        }
        rule.onNodeWithTag("btn").assertIsEnabled().performSemanticsAction(SemanticsActions.OnClick)
        assertEquals(1, clicks)

        loading = true
        rule.onNodeWithTag("btn").assertIsNotEnabled()
        rule.onNodeWithText("Gửi").assertDoesNotExist()
    }

    @Test
    fun priorityChip_showsLabelAndClicks() {
        var picked: Priority? = null
        rule.setContent {
            AppTheme { PriorityChip(Priority.HIGH, onClick = { picked = Priority.HIGH }) }
        }
        rule.onNodeWithText("Cao").performSemanticsAction(SemanticsActions.OnClick)
        assertEquals(Priority.HIGH, picked)
    }

    @Test
    fun taskCard_showsInfoAndClicks() {
        var clicked = false
        val item = TaskWithAssignee(
            task = TaskEntity(id = "t1", projectId = "p1", title = "Viết test", priority = Priority.LOW, status = TaskStatus.DOING),
            assignee = UserEntity(id = "u1", name = "Nguyễn An", email = "an@demo.com", passwordHash = ""),
        )
        rule.setContent {
            AppTheme { TaskCard(item, onClick = { clicked = true }, showStatus = true) }
        }
        rule.onNodeWithText("Viết test").assertIsDisplayed()
        rule.onNodeWithText("Thấp").assertIsDisplayed()
        rule.onNodeWithText("Đang làm").assertIsDisplayed()
        rule.onNodeWithText("NA").assertIsDisplayed()
        rule.onNodeWithText("Viết test").performSemanticsAction(SemanticsActions.OnClick)
        assertEquals(true, clicked)
    }

    @Test
    fun appTextField_passwordToggle_hasVietnameseDescription() {
        rule.setContent { AppTheme { AppTextField("123456", {}, label = "Mật khẩu", isPassword = true) } }
        rule.onNodeWithContentDescription("Hiện mật khẩu").performSemanticsAction(SemanticsActions.OnClick)
        rule.onNodeWithContentDescription("Ẩn mật khẩu").assertIsDisplayed()
    }

    @Test
    fun taskCard_overdue_showsQuaHan() {
        val yesterday = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L
        val item = TaskWithAssignee(
            task = TaskEntity(id = "t2", projectId = "p1", title = "Task trễ hạn", dueDate = yesterday),
            assignee = null,
        )
        rule.setContent { AppTheme { TaskCard(item, onClick = {}) } }
        rule.onNodeWithText("Quá hạn", substring = true).assertIsDisplayed()
    }

    @Test
    fun taskCard_doneTaskWithPastDue_isNotOverdue() {
        val yesterday = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L
        val item = TaskWithAssignee(
            task = TaskEntity(id = "t3", projectId = "p1", title = "Task đã xong", dueDate = yesterday, status = TaskStatus.DONE),
            assignee = null,
        )
        rule.setContent { AppTheme { TaskCard(item, onClick = {}) } }
        rule.onNodeWithText("Hạn", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Quá hạn", substring = true).assertDoesNotExist()
    }
}
