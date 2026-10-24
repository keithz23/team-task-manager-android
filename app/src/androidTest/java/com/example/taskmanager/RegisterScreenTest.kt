package com.example.taskmanager

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.core.ui.LocalSnackbarHostState
import com.example.taskmanager.core.ui.Tags
import com.example.taskmanager.core.ui.theme.AppTheme
import com.example.taskmanager.feature.auth.RegisterScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    private var onBackToLoginClicked = false

    @Before
    fun setUp() {
        hiltRule.inject()
        onBackToLoginClicked = false
        composeRule.setContent {
            AppTheme {
                CompositionLocalProvider(LocalSnackbarHostState provides SnackbarHostState()) {
                    RegisterScreen(
                        onBackToLogin = { onBackToLoginClicked = true }
                    )
                }
            }
        }
    }

    @Test
    fun showsRegisterScreenElements() {
        composeRule.onNodeWithText("Tạo tài khoản").assertIsDisplayed()
        composeRule.onNodeWithText("Bắt đầu quản lý công việc nhóm").assertIsDisplayed()
        composeRule.onNodeWithTag(Tags.FULL_NAME).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(Tags.REGISTER_EMAIL).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(Tags.REGISTER_PASSWORD).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(Tags.CONFIRM_PASSWORD).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(Tags.REGISTER_BUTTON).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Đăng nhập").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun registerValidation_showsErrorsOnEmptySubmit() {
        composeRule.onNodeWithTag(Tags.REGISTER_BUTTON).performSemanticsAction(SemanticsActions.OnClick)

        composeRule.onNodeWithText("Họ tên không được để trống").assertIsDisplayed()
        composeRule.onNodeWithText("Email không được để trống").assertIsDisplayed()
        composeRule.onNodeWithText("Mật khẩu không được để trống").assertIsDisplayed()
        composeRule.onNodeWithText("Vui lòng xác nhận lại mật khẩu").assertIsDisplayed()
    }

    @Test
    fun registerValidation_showsErrorOnPasswordMismatch() {
        composeRule.onNodeWithTag(Tags.FULL_NAME).performTextReplacement("Nguyen Van A")
        composeRule.onNodeWithTag(Tags.REGISTER_EMAIL).performTextReplacement("a@gmail.com")
        composeRule.onNodeWithTag(Tags.REGISTER_PASSWORD).performTextReplacement("123456")
        composeRule.onNodeWithTag(Tags.CONFIRM_PASSWORD).performTextReplacement("654321")
        composeRule.onNodeWithTag(Tags.REGISTER_BUTTON).performSemanticsAction(SemanticsActions.OnClick)

        composeRule.onNodeWithText("Mật khẩu nhập lại không khớp").assertIsDisplayed()
    }

    @Test
    fun clickBackToLogin_triggersCallback() {
        composeRule.onNodeWithText("Đăng nhập").performSemanticsAction(SemanticsActions.OnClick)

        assertTrue(onBackToLoginClicked)
    }
}
