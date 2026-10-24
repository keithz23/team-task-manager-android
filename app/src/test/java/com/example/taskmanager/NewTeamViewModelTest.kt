package com.example.taskmanager

import app.cash.turbine.test
import com.example.taskmanager.feature.team.NewTeamViewModel
import com.example.taskmanager.util.FakeTeamRepository
import com.example.taskmanager.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NewTeamViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val repo = FakeTeamRepository()
    private val vm = NewTeamViewModel(repo)

    @Test
    fun createTeam_blankName_showsErrorAndDoesNotCreate() = runTest {
        var done = false
        vm.onNameChange("   ")
        vm.createTeam { done = true }

        assertEquals("Tên nhóm không được bỏ trống", vm.state.value.nameError)
        assertTrue(repo.createdTeams.isEmpty())
        assertFalse(done)
    }

    @Test
    fun onNameChange_clearsError() = runTest {
        vm.createTeam {}
        vm.onNameChange("Nhóm A")
        assertEquals(null, vm.state.value.nameError)
    }

    @Test
    fun createTeam_success_trimsInputs_addsPendingMembers_andCallsOnDone() = runTest {
        var done = false
        vm.onNameChange("  Nhóm Đồ án  ")
        vm.onFirstProjectChange("  Sprint 1 ")
        vm.onEmailInputChange("binh@demo.com")
        vm.addEmail()

        vm.createTeam { done = true }

        assertEquals(listOf("Nhóm Đồ án" to "Sprint 1"), repo.createdTeams)
        assertEquals(listOf("binh@demo.com"), repo.membersFlow.value["t-1"].orEmpty().map { it.user.email })
        assertTrue(done)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun createTeam_blankFirstProject_passesNull() = runTest {
        vm.onNameChange("Nhóm A")
        vm.onFirstProjectChange("   ")
        vm.createTeam {}
        assertEquals(listOf("Nhóm A" to null), repo.createdTeams)
    }

    @Test
    fun createTeam_failure_sendsMessage_andDoesNotCallOnDone() = runTest {
        repo.createTeamError = IllegalStateException("Tên nhóm đã tồn tại")
        var done = false
        vm.onNameChange("Nhóm A")
        vm.messages.test {
            vm.createTeam { done = true }
            assertEquals("Tên nhóm đã tồn tại", awaitItem())
        }
        assertFalse(done)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun addEmail_normalizesAndClearsInput() = runTest {
        vm.onEmailInputChange("  Binh@Demo.com ")
        vm.addEmail()
        assertEquals(listOf("binh@demo.com"), vm.state.value.pendingEmails)
        assertEquals("", vm.state.value.emailInput)
    }

    @Test
    fun addEmail_duplicate_sendsMessage() = runTest {
        vm.onEmailInputChange("binh@demo.com")
        vm.addEmail()
        vm.onEmailInputChange("binh@demo.com")
        vm.messages.test {
            vm.addEmail()
            assertEquals("Email đã có trong danh sách", awaitItem())
        }
        assertEquals(1, vm.state.value.pendingEmails.size)
    }

    @Test
    fun addEmail_unknownUser_sendsNotFound_andIsNotAdded() = runTest {
        repo.unknownEmails = setOf("ghost@demo.com")
        vm.onEmailInputChange("ghost@demo.com")
        vm.messages.test {
            vm.addEmail()
            assertEquals("Không tìm thấy người dùng với email này", awaitItem())
        }
        assertTrue(vm.state.value.pendingEmails.isEmpty())
    }
}
