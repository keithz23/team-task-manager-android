package com.example.taskmanager

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.core.data.CommentRepository
import com.example.taskmanager.core.data.CommentRepositoryImpl
import com.example.taskmanager.core.database.SeedData
import com.example.taskmanager.core.database.dao.TaskDao
import com.example.taskmanager.core.session.SessionManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/** CommentRepositoryImpl với Room in-memory thật (đã seed SeedData) và session giả. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CommentRepositoryTest {

    @get:Rule val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var repo: CommentRepository
    @Inject lateinit var session: SessionManager
    @Inject lateinit var taskDao: TaskDao

    @Before fun setUp() = hiltRule.inject()

    private suspend fun commentsOfTodo() = repo.observe(SeedData.TASK_TODO).first()

    @Test
    fun observe_returnsCommentsWithAuthor_oldestFirst() = runTest {
        val comments = commentsOfTodo()

        assertEquals(listOf(SeedData.COMMENT_1, SeedData.COMMENT_2), comments.map { it.comment.id })
        assertEquals(listOf(SeedData.USER_AN, SeedData.USER_BINH), comments.map { it.author.id })
        assertTrue(repo.observe(SeedData.TASK_DONE).first().isEmpty())
    }

    @Test
    fun add_whenLoggedIn_savesTrimmedCommentByCurrentUser() = runTest {
        session.save(SeedData.USER_BINH)

        val result = repo.add(SeedData.TASK_TODO, "  Đã xong phần form  ")

        assertTrue(result.isSuccess)
        val last = commentsOfTodo().last()
        assertEquals("Đã xong phần form", last.comment.content)
        assertEquals(SeedData.USER_BINH, last.author.id)
        assertEquals(3, commentsOfTodo().size)
    }

    @Test
    fun add_blank_failsAndSavesNothing() = runTest {
        session.save(SeedData.USER_AN)

        val result = repo.add(SeedData.TASK_TODO, "   ")

        assertTrue(result.isFailure)
        assertEquals("Bình luận không được để trống", result.exceptionOrNull()?.message)
        assertEquals(2, commentsOfTodo().size)
    }

    @Test
    fun add_tooLong_fails() = runTest {
        session.save(SeedData.USER_AN)

        val result = repo.add(SeedData.TASK_TODO, "a".repeat(CommentRepositoryImpl.MAX_LENGTH + 1))

        assertTrue(result.isFailure)
        assertEquals(2, commentsOfTodo().size)
    }

    @Test
    fun add_whenLoggedOut_fails() = runTest {
        val result = repo.add(SeedData.TASK_TODO, "Xin chào")

        assertTrue(result.isFailure)
        assertEquals("Chưa đăng nhập", result.exceptionOrNull()?.message)
        assertEquals(2, commentsOfTodo().size)
    }

    @Test
    fun deletingTask_deletesItsComments() = runTest {
        taskDao.delete(SeedData.TASK_TODO)

        assertTrue(commentsOfTodo().isEmpty())
    }
}
