package com.example.taskmanager

import app.cash.turbine.test
import com.example.taskmanager.feature.profile.ProfileViewModel
import com.example.taskmanager.util.FakeAuthRepository
import com.example.taskmanager.util.FakeTaskRepository
import com.example.taskmanager.util.FakeTeamRepository
import com.example.taskmanager.util.MainDispatcherRule
import com.example.taskmanager.util.testUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val userAn = testUser("u-an", "Nguyễn An")
    private val authRepo = FakeAuthRepository(initialUsers = listOf(userAn), initialCurrentUserId = userAn.id)
    private val teamRepo = FakeTeamRepository()
    private val taskRepo = FakeTaskRepository()

    private fun TestScope.createVm(): ProfileViewModel {
        val vm = ProfileViewModel(authRepo, teamRepo, taskRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.currentUserId.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.teamCount.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.completedTaskCount.collect {}
        }
        return vm
    }

    // --- CURRENT USER ID TESTS ---

    @Test
    fun currentUserId_initiallyMatchesAuthRepo_andUpdatesOnSessionChange() = runTest {
        val vm = createVm()

        assertEquals("u-an", vm.currentUserId.value)

        authRepo.logout()
        advanceUntilIdle()

        assertNull(vm.currentUserId.value)
    }

    // --- GET USER BY ID TESTS ---

    @Test
    fun getUserById_success_updatesUserAndResetsLoading() = runTest {
        val vm = createVm()

        vm.getUserById("u-an")
        advanceUntilIdle()

        assertEquals(userAn, vm.user)
        assertFalse(vm.isLoading)
    }

    @Test
    fun getUserById_failure_emitsErrorMessage() = runTest {
        val vm = createVm()

        vm.messages.test {
            vm.getUserById("non-existent-id")
            advanceUntilIdle()

            assertEquals("Không tìm thấy người dùng", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertNull(vm.user)
        assertFalse(vm.isLoading)
    }

    @Test
    fun getUserById_blankUserId_doesNothing() = runTest {
        val vm = createVm()

        vm.getUserById("   ")
        advanceUntilIdle()

        assertNull(vm.user)
        assertFalse(vm.isLoading)
    }

    @Test
    fun getUserById_customFailureMessage_emitsCustomError() = runTest {
        authRepo.failGetUserMsg = "Lỗi kết nối cơ sở dữ liệu"
        val vm = createVm()

        vm.messages.test {
            vm.getUserById("u-an")
            advanceUntilIdle()

            assertEquals("Lỗi kết nối cơ sở dữ liệu", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertNull(vm.user)
        assertFalse(vm.isLoading)
    }

    // --- LOGOUT TESTS ---

    @Test
    fun logout_clearsUserSessionInAuthRepo() = runTest {
        val vm = createVm()
        assertEquals("u-an", vm.currentUserId.value)

        vm.logout()
        advanceUntilIdle()

        assertNull(authRepo.currentUserIdFlow.value)
        assertNull(vm.currentUserId.value)
    }

    // --- TEAM COUNT TESTS ---

    @Test
    fun teamCount_loggedIn_emitsValueFromTeamRepo() = runTest {
        teamRepo.teamCountFlow.value = 4
        val vm = createVm()

        advanceUntilIdle()

        assertEquals(4, vm.teamCount.value)
    }

    @Test
    fun teamCount_loggedOut_emitsZero() = runTest {
        authRepo.logout()
        teamRepo.teamCountFlow.value = 5
        val vm = createVm()

        advanceUntilIdle()

        assertEquals(0, vm.teamCount.value)
    }

    @Test
    fun teamCount_repoException_emitsZeroAndSendsErrorMessage() = runTest {
        teamRepo.teamCountError = RuntimeException("Không kết nối được server")
        val vm = ProfileViewModel(authRepo, teamRepo, taskRepo)

        vm.messages.test {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.teamCount.collect {}
            }
            advanceUntilIdle()

            assertEquals("Không kết nối được server", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(0, vm.teamCount.value)
    }

    // --- COMPLETED TASK COUNT TESTS ---

    @Test
    fun completedTaskCount_loggedIn_emitsValueFromTaskRepo() = runTest {
        taskRepo.completedTaskCountFlow.value = 12
        val vm = createVm()

        advanceUntilIdle()

        assertEquals(12, vm.completedTaskCount.value)
    }

    @Test
    fun completedTaskCount_loggedOut_emitsZero() = runTest {
        authRepo.logout()
        taskRepo.completedTaskCountFlow.value = 10
        val vm = createVm()

        advanceUntilIdle()

        assertEquals(0, vm.completedTaskCount.value)
    }

    @Test
    fun completedTaskCount_repoException_emitsZeroAndSendsErrorMessage() = runTest {
        taskRepo.completedTaskCountError = RuntimeException("Lỗi truy vấn task")
        val vm = ProfileViewModel(authRepo, teamRepo, taskRepo)

        vm.messages.test {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.completedTaskCount.collect {}
            }
            advanceUntilIdle()

            assertEquals("Lỗi truy vấn task", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(0, vm.completedTaskCount.value)
    }
}