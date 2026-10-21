package com.example.taskmanager

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.core.database.SeedData
import com.example.taskmanager.core.session.SessionManager
import com.example.taskmanager.feature.auth.AuthRepository
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

/** AuthRepositoryImpl với Room in-memory thật (đã seed SeedData) và session giả. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AuthRepositoryTest {

    @get:Rule val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var repo: AuthRepository
    @Inject lateinit var session: SessionManager

    @Before
    fun setUp() = hiltRule.inject()

    @Test
    fun login_validCredentials_succeedsAndUpdatesSession() = runTest {
        // SeedData: email an@demo.com, pass: 123456
        val result = repo.login(SeedData.EMAIL_AN, SeedData.PASSWORD)

        assertTrue(result.isSuccess)
        assertEquals(SeedData.USER_AN, repo.currentUserId.first())
    }

    @Test
    fun login_wrongPassword_fails() = runTest {
        val result = repo.login(SeedData.EMAIL_AN, "wrongpassword")

        assertTrue(result.isFailure)
        assertEquals("Email hoặc mật khẩu không đúng", result.exceptionOrNull()?.message)
    }

    @Test
    fun login_nonExistentEmail_fails() = runTest {
        val result = repo.login("nonexistent@example.com", "123456")

        assertTrue(result.isFailure)
        assertEquals("Email hoặc mật khẩu không đúng", result.exceptionOrNull()?.message)
    }

    @Test
    fun register_newValidUser_succeedsAndSavesSession() = runTest {
        val name = "Lê Văn C"
        val email = "levanc@gmail.com"
        val pass = "123456"

        val result = repo.register(name, email, pass)

        assertTrue(result.isSuccess)

        val currentUserId = repo.currentUserId.first()
        assertTrue(currentUserId != null)

        val userResult = repo.getUser(currentUserId!!)
        assertTrue(userResult.isSuccess)
        assertEquals(name, userResult.getOrNull()?.name)
        assertEquals(email, userResult.getOrNull()?.email)
    }

    @Test
    fun register_existingEmail_fails() = runTest {
        // an@demo.com đã có trong SeedData
        val result = repo.register("Nguyễn An Trùng", SeedData.EMAIL_AN, "123456")

        assertTrue(result.isFailure)
        assertEquals("Email đã được sử dụng", result.exceptionOrNull()?.message)
    }

    @Test
    fun getUser_existingUser_returnsUserEntity() = runTest {
        val userResult = repo.getUser(SeedData.USER_AN)

        assertTrue(userResult.isSuccess)
        assertEquals("Nguyễn An", userResult.getOrNull()?.name)
        assertEquals(SeedData.EMAIL_AN, userResult.getOrNull()?.email)
    }

    @Test
    fun getUser_nonExistentUser_fails() = runTest {
        val userResult = repo.getUser("user-does-not-exist")

        assertTrue(userResult.isFailure)
        assertEquals("Không tìm thấy người dùng", userResult.exceptionOrNull()?.message)
    }

    @Test
    fun logout_clearsCurrentUserId() = runTest {
        session.save(SeedData.USER_AN)
        assertEquals(SeedData.USER_AN, repo.currentUserId.first())

        repo.logout()

        assertNull(repo.currentUserId.first())
    }
}
