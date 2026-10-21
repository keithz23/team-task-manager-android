package com.example.taskmanager.util

import app.cash.turbine.test
import com.example.taskmanager.core.session.AuthState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class FakeSessionManagerTest {

    @get:Rule val mainRule = MainDispatcherRule()

    @Test
    fun defaultIsLoggedOut() = runTest {
        val session = FakeSessionManager()

        assertNull(session.currentUserId())
        session.authState.test { assertEquals(AuthState.LoggedOut, awaitItem()) }
    }

    @Test
    fun initialUserIdIsLoggedIn() = runTest {
        val session = FakeSessionManager("u1")

        assertEquals("u1", session.currentUserId())
        session.authState.test { assertEquals(AuthState.LoggedIn("u1"), awaitItem()) }
    }

    @Test
    fun saveAndClearEmitNewStates() = runTest {
        val session = FakeSessionManager()

        session.authState.test {
            assertEquals(AuthState.LoggedOut, awaitItem())
            session.save("u2")
            assertEquals(AuthState.LoggedIn("u2"), awaitItem())
            session.clear()
            assertEquals(AuthState.LoggedOut, awaitItem())
        }
    }

    @Test
    fun mainDispatcherIsReplaced() = runTest {
        var ran = false
        // Không có rule thì Dispatchers.Main ném lỗi trên JVM.
        withContext(Dispatchers.Main) { launch { ran = true } }
        assertEquals(true, ran)
    }
}
