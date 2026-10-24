package com.example.taskmanager

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.core.database.SeedData
import com.example.taskmanager.core.session.SessionManager
import com.example.taskmanager.core.ui.LocalSnackbarHostState
import com.example.taskmanager.core.ui.Tags
import com.example.taskmanager.core.ui.theme.AppTheme
import com.example.taskmanager.feature.team.NewTeamScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NewTeamScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var session: SessionManager

    private var onDoneCalled = false

    @Before
    fun setUp() {
        hiltRule.inject()
        onDoneCalled = false
        runBlocking { session.save(SeedData.USER_AN) }

        composeRule.setContent {
            AppTheme {
                CompositionLocalProvider(LocalSnackbarHostState provides SnackbarHostState()) {
                    NewTeamScreen(onDone = { onDoneCalled = true })
                }
            }
        }
    }

    @Test
    fun showsNewTeamScreenElements() {
        composeRule.onNodeWithText("Tạo nhóm mới").assertIsDisplayed()
        composeRule.onNodeWithTag(Tags.TEAM_NAME).assertIsDisplayed()
        composeRule.onNodeWithTag(Tags.CREATE_TEAM_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithText("Bạn").assertIsDisplayed()
        composeRule.onNodeWithText("Chủ nhóm").assertIsDisplayed()
    }

    @Test
    fun createTeam_emptyName_showsValidationError() {
        // Click trực tiếp vào nút Tạo nhóm
        composeRule.onNodeWithTag(Tags.CREATE_TEAM_BUTTON)
            .performClick()

        // Chờ Compose cập nhật state hiển thị lỗi
        composeRule.waitForIdle()

        // Kiểm tra thông báo lỗi hiển thị
        composeRule.onNodeWithText("Tên nhóm không được bỏ trống")
            .assertIsDisplayed()
    }

    @Test
    fun createTeam_validName_triggersOnDone() {
        composeRule.onNodeWithTag(Tags.TEAM_NAME).performTextReplacement("Nhóm DOFPK")
        composeRule.onNodeWithTag(Tags.CREATE_TEAM_BUTTON)
            .performSemanticsAction(SemanticsActions.OnClick)

        composeRule.waitUntil(timeoutMillis = 5_000) { onDoneCalled }
        assertTrue(onDoneCalled)
    }

    @Test
    fun clickBack_triggersOnDone() {
        composeRule.onNodeWithContentDescription("Quay lại")
            .performClick()

        assertTrue(onDoneCalled)
    }
}
