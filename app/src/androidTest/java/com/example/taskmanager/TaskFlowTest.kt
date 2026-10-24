package com.example.taskmanager

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.core.database.SeedData
import com.example.taskmanager.core.session.SessionManager
import com.example.taskmanager.core.ui.Tags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Luồng cốt lõi trên MainActivity thật: tạo công việc → thấy ở tab "Cần làm" → mở chi tiết →
 * đổi sang "Đang làm" → quay lại Board (Espresso Back) → công việc đã chuyển sang tab "Đang làm".
 * Dữ liệu ban đầu từ SeedData: project có 1 task ở mỗi trạng thái.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TaskFlowTest {

    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject lateinit var session: SessionManager

    @Before
    fun setUp() {
        hiltRule.inject()
        runBlocking { session.save(SeedData.USER_AN) } // đăng nhập sẵn, luồng đăng nhập có test riêng
    }

    private fun exists(text: String) =
        composeRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()

    private fun waitForText(text: String) = composeRule.waitUntil(TIMEOUT) { exists(text) }

    private fun click(text: String) =
        composeRule.onNodeWithText(text).performSemanticsAction(SemanticsActions.OnClick)

    @Test
    fun createTask_thenMoveToDoing_showsInDoingTab() {
        // Nhóm → Board của project mẫu
        waitForText("App quản lý công việc")
        click("App quản lý công việc")
        waitForText("Tạo Task mới")
        waitForText("1 task") // tab Cần làm có sẵn 1 task từ SeedData

        // Tạo công việc mới
        click("Tạo Task mới")
        composeRule.waitUntil(TIMEOUT) {
            composeRule.onAllNodes(hasTestTag(Tags.TASK_TITLE)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(Tags.TASK_TITLE).performTextInput(TITLE)
        click("Tạo mới")

        // Hiện ở tab Cần làm (đang chọn), số lượng tab tăng lên 2
        waitForText(TITLE)
        composeRule.onNodeWithText(TITLE).assertIsDisplayed()
        waitForText("2 task")

        // Mở chi tiết, đổi sang Đang làm
        click(TITLE)
        waitForText("Chi tiết công việc")
        waitForText("Trạng thái")
        click("Đang làm")

        // Quay lại Board bằng nút Back hệ thống
        Espresso.pressBack()
        waitForText("Tạo Task mới")

        // Không còn ở tab Cần làm, đã có ở tab Đang làm
        composeRule.waitUntil(TIMEOUT) { !exists(TITLE) }
        click("Đang làm")
        waitForText(TITLE)
        composeRule.onNodeWithText(TITLE).assertIsDisplayed()
    }

    private companion object {
        const val TIMEOUT = 15_000L
        const val TITLE = "Viết báo cáo cuối kỳ"
    }
}
