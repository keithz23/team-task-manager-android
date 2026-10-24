package com.example.taskmanager

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.core.database.SeedData
import com.example.taskmanager.core.session.SessionManager
import com.example.taskmanager.core.ui.Tags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Test đầu-cuối trên MainActivity thật (DB in-memory đã seed, session giả, xem di/TestModules.kt).
 * Dùng Espresso cho nút Back hệ thống: sau khi đăng nhập/đăng xuất, Back không được đưa người dùng
 * quay lại màn hình trước đó (Login sau khi đã đăng nhập, hoặc vào trong app sau khi đã đăng xuất).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginLogoutFlowTest {

    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject lateinit var session: SessionManager

    @Before fun setUp() = hiltRule.inject()

    private fun waitForText(text: String) = composeRule.waitUntil(TIMEOUT) {
        composeRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
    }

    private fun waitForTag(tag: String) = composeRule.waitUntil(TIMEOUT) {
        composeRule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()
    }

    private fun loginAsAn() {
        waitForTag(Tags.EMAIL)
        composeRule.onNodeWithTag(Tags.EMAIL).performTextInput(SeedData.EMAIL_AN)
        composeRule.onNodeWithTag(Tags.PASSWORD).performTextInput(SeedData.PASSWORD)
        composeRule.onNodeWithTag(Tags.LOGIN_BUTTON).performSemanticsAction(SemanticsActions.OnClick)
        waitForText("Team Demo") // vào được màn Nhóm, thấy nhóm từ SeedData
    }

    /** Back ở màn gốc: activity rời màn hình (Android 12+ chỉ đưa app xuống nền, không nhất thiết huỷ). */
    private fun assertBackLeavesApp() {
        Espresso.pressBackUnconditionally()
        composeRule.waitUntil(TIMEOUT) {
            composeRule.activityRule.scenario.state != Lifecycle.State.RESUMED
        }
        assertNotEquals(Lifecycle.State.RESUMED, composeRule.activityRule.scenario.state)
    }

    @Test
    fun login_success_opensTeams_andBackDoesNotReturnToLogin() {
        loginAsAn()

        assertEquals(SeedData.USER_AN, runBlocking { session.currentUserId() })
        composeRule.onNodeWithText("Nhóm của tôi").assertExists()

        assertBackLeavesApp()
    }

    @Test
    fun logout_returnsToLogin_andBackDoesNotReenterApp() {
        loginAsAn()

        composeRule.onNodeWithText("Cá nhân").performSemanticsAction(SemanticsActions.OnClick)
        waitForTag("logout_button")
        composeRule.onNodeWithTag("logout_button").performSemanticsAction(SemanticsActions.OnClick)
        // Nút xác nhận trong hộp thoại (khác nút "Đăng xuất" trên màn Profile)
        composeRule.onNode(hasText("Đăng xuất") and hasClickAction() and hasAnyAncestor(isDialog()))
            .performSemanticsAction(SemanticsActions.OnClick)

        waitForTag(Tags.LOGIN_BUTTON)
        assertNull(runBlocking { session.currentUserId() })

        assertBackLeavesApp()
    }

    private companion object {
        const val TIMEOUT = 15_000L
    }
}
