package com.example.taskmanager

import app.cash.turbine.test
import com.example.taskmanager.core.database.entity.UserEntity
import com.example.taskmanager.feature.auth.AuthViewModel
import com.example.taskmanager.util.FakeAuthRepository
import com.example.taskmanager.util.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val userAn = UserEntity(
        id = "u-an",
        name = "Nguyễn An",
        email = "an@gmail.com",
        passwordHash = "hash123"
    )

    private val repo = FakeAuthRepository(initialUsers = listOf(userAn))

    private fun TestScope.createVm(): AuthViewModel {
        val vm = AuthViewModel(repo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.currentUserId.collect {}
        }
        return vm
    }

    // --- LOGIN VALIDATION TESTS ---

    @Test
    fun login_blankEmail_showsEmailError() = runTest {
        val vm = createVm()
        vm.loginEmail = ""
        vm.loginPassword = "password123"

        vm.login()

        assertEquals("Email không được để trống", vm.emailError)
        assertNull(vm.passwordError)
        assertTrue(repo.loginCalls.isEmpty())
    }

    @Test
    fun login_invalidEmailFormat_showsEmailError() = runTest {
        val vm = createVm()
        vm.loginEmail = "invalid-email"
        vm.loginPassword = "password123"

        vm.login()

        assertEquals("Định dạng email không hợp lệ", vm.emailError)
        assertNull(vm.passwordError)
        assertTrue(repo.loginCalls.isEmpty())
    }

    @Test
    fun login_blankPassword_showsPasswordError() = runTest {
        val vm = createVm()
        vm.loginEmail = "an@gmail.com"
        vm.loginPassword = ""

        vm.login()

        assertNull(vm.emailError)
        assertEquals("Mật khẩu không được để trống", vm.passwordError)
        assertTrue(repo.loginCalls.isEmpty())
    }

    @Test
    fun login_shortPassword_showsPasswordError() = runTest {
        val vm = createVm()
        vm.loginEmail = "an@gmail.com"
        vm.loginPassword = "123"

        vm.login()

        assertNull(vm.emailError)
        assertEquals("Mật khẩu phải có ít nhất 6 ký tự", vm.passwordError)
        assertTrue(repo.loginCalls.isEmpty())
    }

    // --- LOGIN EXECUTION & STATE TESTS ---

    @Test
    fun login_validCredentials_callsRepoLoginAndUpdatesSession() = runTest {
        val vm = createVm()
        vm.loginEmail = "an@gmail.com"
        vm.loginPassword = "password123"

        vm.login()
        advanceUntilIdle()

        assertNull(vm.emailError)
        assertNull(vm.passwordError)
        assertFalse(vm.isLoading)
        assertEquals(listOf("an@gmail.com" to "password123"), repo.loginCalls)
        assertEquals("u-an", vm.currentUserId.value)
    }

    @Test
    fun login_repoFailure_emitsErrorMessage() = runTest {
        val vm = createVm()
        repo.failLoginMsg = "Email hoặc mật khẩu không đúng"
        vm.loginEmail = "an@gmail.com"
        vm.loginPassword = "wrongpassword"

        vm.messages.test {
            vm.login()
            advanceUntilIdle()

            assertEquals("Email hoặc mật khẩu không đúng", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(vm.isLoading)
    }

    @Test
    fun login_whileLoading_ignoresMultipleCalls() = runTest {
        val gate = CompletableDeferred<Unit>()
        repo.gate = gate
        val vm = createVm()
        vm.loginEmail = "an@gmail.com"
        vm.loginPassword = "password123"

        vm.login()
        assertTrue(vm.isLoading)

        // Bấm login lần thứ 2 khi đang loading
        vm.login()

        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(1, repo.loginCalls.size)
        assertFalse(vm.isLoading)
    }

    // --- REGISTER VALIDATION TESTS ---

    @Test
    fun register_blankFullName_showsFullNameError() = runTest {
        val vm = createVm()
        vm.registerFullName = "   "
        vm.registerEmail = "binh@gmail.com"
        vm.registerPassword = "password123"
        vm.registerConfirmPassword = "password123"

        vm.register()

        assertEquals("Họ tên không được để trống", vm.fullNameError)
        assertTrue(repo.registerCalls.isEmpty())
    }

    @Test
    fun register_blankEmail_showsEmailError() = runTest {
        val vm = createVm()
        vm.registerFullName = "Trần Bình"
        vm.registerEmail = ""
        vm.registerPassword = "password123"
        vm.registerConfirmPassword = "password123"

        vm.register()

        assertEquals("Email không được để trống", vm.emailError)
        assertTrue(repo.registerCalls.isEmpty())
    }

    @Test
    fun register_invalidEmail_showsEmailError() = runTest {
        val vm = createVm()
        vm.registerFullName = "Trần Bình"
        vm.registerEmail = "not-an-email"
        vm.registerPassword = "password123"
        vm.registerConfirmPassword = "password123"

        vm.register()

        assertEquals("Định dạng email không hợp lệ", vm.emailError)
        assertTrue(repo.registerCalls.isEmpty())
    }

    @Test
    fun register_blankPassword_showsPasswordError() = runTest {
        val vm = createVm()
        vm.registerFullName = "Trần Bình"
        vm.registerEmail = "binh@gmail.com"
        vm.registerPassword = ""
        vm.registerConfirmPassword = "password123"

        vm.register()

        assertEquals("Mật khẩu không được để trống", vm.passwordError)
        assertTrue(repo.registerCalls.isEmpty())
    }

    @Test
    fun register_shortPassword_showsPasswordError() = runTest {
        val vm = createVm()
        vm.registerFullName = "Trần Bình"
        vm.registerEmail = "binh@gmail.com"
        vm.registerPassword = "123"
        vm.registerConfirmPassword = "123"

        vm.register()

        assertEquals("Mật khẩu phải có ít nhất 6 ký tự", vm.passwordError)
        assertTrue(repo.registerCalls.isEmpty())
    }

    @Test
    fun register_blankConfirmPassword_showsConfirmPasswordError() = runTest {
        val vm = createVm()
        vm.registerFullName = "Trần Bình"
        vm.registerEmail = "binh@gmail.com"
        vm.registerPassword = "password123"
        vm.registerConfirmPassword = ""

        vm.register()

        assertEquals("Vui lòng xác nhận lại mật khẩu", vm.confirmPasswordError)
        assertTrue(repo.registerCalls.isEmpty())
    }

    @Test
    fun register_mismatchedPassword_showsConfirmPasswordError() = runTest {
        val vm = createVm()
        vm.registerFullName = "Trần Bình"
        vm.registerEmail = "binh@gmail.com"
        vm.registerPassword = "password123"
        vm.registerConfirmPassword = "password456"

        vm.register()

        assertEquals("Mật khẩu nhập lại không khớp", vm.confirmPasswordError)
        assertTrue(repo.registerCalls.isEmpty())
    }

    // --- REGISTER EXECUTION & STATE TESTS ---

    @Test
    fun register_validData_callsRepoRegisterAndUpdatesSession() = runTest {
        val vm = createVm()
        vm.registerFullName = "Trần Bình"
        vm.registerEmail = "binh@gmail.com"
        vm.registerPassword = "password123"
        vm.registerConfirmPassword = "password123"

        vm.register()
        advanceUntilIdle()

        assertNull(vm.fullNameError)
        assertNull(vm.emailError)
        assertNull(vm.passwordError)
        assertNull(vm.confirmPasswordError)
        assertFalse(vm.isLoading)

        assertEquals(1, repo.registerCalls.size)
        assertEquals("Trần Bình", repo.registerCalls[0].first)
        assertEquals("binh@gmail.com", repo.registerCalls[0].second)
        assertEquals("password123", repo.registerCalls[0].third)
        assertEquals("u-2", vm.currentUserId.value)
    }

    @Test
    fun register_repoFailure_emitsErrorMessage() = runTest {
        val vm = createVm()
        repo.failRegisterMsg = "Email đã được sử dụng"
        vm.registerFullName = "Trần Bình"
        vm.registerEmail = "an@gmail.com"
        vm.registerPassword = "password123"
        vm.registerConfirmPassword = "password123"

        vm.messages.test {
            vm.register()
            advanceUntilIdle()

            assertEquals("Email đã được sử dụng", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(vm.isLoading)
    }

    // --- USER & SESSION TESTS ---

    @Test
    fun getUserById_success_updatesUserState() = runTest {
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
            vm.getUserById("non-existent-user")
            advanceUntilIdle()

            assertEquals("Không tìm thấy người dùng", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertNull(vm.user)
        assertFalse(vm.isLoading)
    }

    @Test
    fun getUserById_blankId_doesNothing() = runTest {
        val vm = createVm()

        vm.getUserById("   ")
        advanceUntilIdle()

        assertNull(vm.user)
        assertFalse(vm.isLoading)
    }

    @Test
    fun logout_clearsCurrentUserId() = runTest {
        val vm = createVm()
        repo.currentUserIdFlow.value = "u-an"

        vm.logout()
        advanceUntilIdle()

        assertNull(vm.currentUserId.value)
    }

    @Test
    fun clearError_resetsErrorState() = runTest {
        val vm = createVm()

        vm.clearError()

        assertNull(vm.error)
    }
}
