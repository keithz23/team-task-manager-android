package com.example.taskmanager

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.core.ui.LocalSnackbarHostState
import com.example.taskmanager.core.ui.Tags
import com.example.taskmanager.core.ui.theme.AppTheme
import com.example.taskmanager.feature.auth.LoginScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    private var onRegisterClicked = false

    @Before
    fun setUp() {
        hiltRule.inject()
        onRegisterClicked = false

        composeRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }

            AppTheme {
                CompositionLocalProvider(
                    LocalSnackbarHostState provides snackbarHostState
                ) {
                    LoginScreen(
                        onRegister = { onRegisterClicked = true }
                    )
                }
            }
        }
    }

    @Test
    fun showsLoginScreenElements() {
        composeRule.onNodeWithText("Task Manager").assertIsDisplayed()
        composeRule.onNodeWithText("Quản lý công việc nhóm").assertIsDisplayed()
        composeRule.onNodeWithTag(Tags.EMAIL).assertIsDisplayed()
        composeRule.onNodeWithTag(Tags.PASSWORD).assertIsDisplayed()
        composeRule.onNodeWithTag(Tags.LOGIN_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithText("Đăng ký").assertIsDisplayed()
    }

    @Test
    fun loginValidation_showsErrorsOnEmptySubmit() {
        composeRule.onNodeWithTag(Tags.LOGIN_BUTTON)
            .performSemanticsAction(SemanticsActions.OnClick)

        composeRule.onNodeWithText("Email không được để trống").assertIsDisplayed()
        composeRule.onNodeWithText("Mật khẩu không được để trống").assertIsDisplayed()
    }

    @Test
    fun loginValidation_showsErrorOnInvalidEmail() {
        composeRule.onNodeWithTag(Tags.EMAIL).performTextReplacement("invalid-email")
        composeRule.onNodeWithTag(Tags.PASSWORD).performTextReplacement("123456")
        composeRule.onNodeWithTag(Tags.LOGIN_BUTTON)
            .performSemanticsAction(SemanticsActions.OnClick)

        composeRule.onNodeWithText("Định dạng email không hợp lệ").assertIsDisplayed()
    }

    @Test
    fun clickRegister_triggersCallback() {
        composeRule.onNodeWithText("Đăng ký").performSemanticsAction(SemanticsActions.OnClick)

        assertTrue(onRegisterClicked)
    }
}
