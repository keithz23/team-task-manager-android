package com.example.taskmanager

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.core.database.SeedData
import com.example.taskmanager.core.session.SessionManager
import com.example.taskmanager.core.ui.Tags
import com.example.taskmanager.core.ui.theme.AppTheme
import com.example.taskmanager.navigation.Board
import com.example.taskmanager.navigation.navigation.taskGraph
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BoardScreenTest {

    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject lateinit var session: SessionManager

    @Before
    fun setUp() {
        hiltRule.inject()
        // Đăng nhập sẵn tài khoản An
        runBlocking { session.save(SeedData.USER_AN) }

        // Khởi chạy giao diện BoardScreen thông qua NavHost thật
        composeRule.setContent {
            AppTheme {
                val nav = rememberNavController()
                NavHost(
                    navController = nav,
                    startDestination = Board(projectId = SeedData.PROJECT_ID, teamId = SeedData.TEAM_ID)
                ) {
                    taskGraph(nav)
                }
            }
        }
    }

    // 1. Kiểm tra hiển thị tiêu đề dự án, các tab và task mặc định Todo
    @Test
    fun showsProjectNameAndDefaultTodoTab() {
        // Tên dự án
        composeRule.onNodeWithText("App quản lý công việc").assertIsDisplayed()

        // Nút Tạo task mới
        composeRule.onNodeWithText("Tạo Task mới").assertIsDisplayed()

        // 3 Tab Todo, Doing, Done
        composeRule.onNodeWithText("Todo").assertIsDisplayed()
        composeRule.onNodeWithText("Doing").assertIsDisplayed()
        composeRule.onNodeWithText("Done").assertIsDisplayed()

        // Task mẫu Todo từ SeedData
        composeRule.onNodeWithText("Thiết kế màn hình đăng nhập").assertIsDisplayed()
    }

    // 2. Chuyển sang tab Doing thì hiển thị task của tab Doing
    @Test
    fun clickDoingTab_showsDoingTask() {
        composeRule.onNodeWithText("Doing").performSemanticsAction(SemanticsActions.OnClick)

        composeRule.onNodeWithText("Viết API tạo task").assertIsDisplayed()
    }

    // 3. Chuyển sang tab Done thì hiển thị task của tab Done
    @Test
    fun clickDoneTab_showsDoneTask() {
        composeRule.onNodeWithText("Done").performSemanticsAction(SemanticsActions.OnClick)

        composeRule.onNodeWithText("Khởi tạo project").assertIsDisplayed()
    }

    // 4. Bấm nút "Tạo Task mới" thì mở TaskFormSheet
    @Test
    fun clickCreateTaskButton_opensTaskFormSheet() {
        composeRule
            .onNodeWithText("Tạo Task mới")
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodesWithTag(Tags.TASK_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule
            .onNodeWithTag(Tags.TASK_TITLE)
            .assertIsDisplayed()
    }
}
