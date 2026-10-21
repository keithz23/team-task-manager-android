package com.example.taskmanager

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.core.database.SeedData
import com.example.taskmanager.core.session.SessionManager
import com.example.taskmanager.core.ui.Tags
import com.example.taskmanager.core.ui.theme.AppTheme
import com.example.taskmanager.feature.comment.CommentSection
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * UI test "Thêm bình luận": CommentSection thật + ViewModel thật + Room in-memory (SeedData),
 * đăng nhập sẵn bằng tài khoản An.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddCommentTest {

    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject lateinit var session: SessionManager

    @Before
    fun setUp() {
        hiltRule.inject()
        runBlocking { session.save(SeedData.USER_AN) }
        composeRule.setContent { AppTheme { CommentSection(SeedData.TASK_TODO) } }
    }

    private fun inCommentList(text: String) =
        hasText(text) and hasAnyAncestor(hasTestTag(Tags.COMMENT_LIST))

    @Test
    fun showsSeededComments() {
        composeRule.onNodeWithText("Bình luận (2)").assertIsDisplayed()
        composeRule.onNodeWithTag(Tags.commentItem(SeedData.COMMENT_1)).assertIsDisplayed()
        composeRule.onNodeWithText("Mình bắt đầu làm nhé").assertIsDisplayed()
        composeRule.onNodeWithText("Nguyễn An (bạn)").assertIsDisplayed()
        composeRule.onNodeWithText("Trần Bình").assertIsDisplayed()
    }

    @Test
    fun addComment_appearsInList_andInputIsCleared() {
        val message = "Mình đã push code lên rồi"

        composeRule.onNodeWithTag(Tags.COMMENT_SEND).assertIsNotEnabled()
        composeRule.onNodeWithTag(Tags.COMMENT_INPUT).performTextInput(message)
        composeRule.onNodeWithTag(Tags.COMMENT_SEND).assertIsEnabled().performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(inCommentList(message)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(inCommentList(message)).assertIsDisplayed()
        composeRule.onNodeWithText("Bình luận (3)").assertIsDisplayed()
        // Ô nhập đã được xoá nên nút gửi bị khoá lại.
        composeRule.onNodeWithTag(Tags.COMMENT_SEND).assertIsNotEnabled()
    }
}
