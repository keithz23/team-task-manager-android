package com.example.taskmanager

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.core.database.SeedData
import com.example.taskmanager.core.database.dao.CommentDao
import com.example.taskmanager.core.database.dao.UserDao
import com.example.taskmanager.core.security.PasswordHasher
import com.example.taskmanager.core.session.AuthState
import com.example.taskmanager.core.session.SessionManager
import com.example.taskmanager.util.FakeSessionManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/** Kiểm tra hạ tầng test: Hilt thay module, DB in-memory đã seed, session giả hoạt động. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class InfraSmokeTest {

    @get:Rule val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var userDao: UserDao
    @Inject lateinit var commentDao: CommentDao
    @Inject lateinit var hasher: PasswordHasher
    @Inject lateinit var session: SessionManager

    @Before fun setUp() = hiltRule.inject()

    @Test
    fun seedDataIsLoaded() = runTest {
        val an = userDao.findByEmail(SeedData.EMAIL_AN)
        assertEquals(SeedData.USER_AN, an?.id)
        assertTrue(hasher.verify(SeedData.PASSWORD, an!!.passwordHash))
        assertEquals(SeedData.USER_BINH, userDao.findByEmail(SeedData.EMAIL_BINH)?.id)

        val comments = commentDao.observe(SeedData.TASK_TODO).first()
        assertEquals(listOf(SeedData.COMMENT_1, SeedData.COMMENT_2), comments.map { it.comment.id })
    }

    @Test
    fun sessionIsFakeAndStartsLoggedOut() = runTest {
        assertTrue(session is FakeSessionManager)
        assertNull(session.currentUserId())
        assertEquals(AuthState.LoggedOut, session.authState.first())

        session.save(SeedData.USER_AN)
        assertEquals(SeedData.USER_AN, session.currentUserId())
        assertEquals(AuthState.LoggedIn(SeedData.USER_AN), session.authState.first())
    }
}
