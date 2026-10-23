package com.example.taskmanager

import androidx.lifecycle.SavedStateHandle
import com.example.taskmanager.core.database.entity.TeamMemberEntity
import com.example.taskmanager.core.database.entity.UserEntity
import com.example.taskmanager.core.database.relation.MemberWithUser
import com.example.taskmanager.core.database.relation.ProjectWithCount
import com.example.taskmanager.core.database.relation.TeamWithCounts
import com.example.taskmanager.core.model.TeamRole
import com.example.taskmanager.feature.team.TeamDetailViewModel
import com.example.taskmanager.util.FakeTeamRepository
import com.example.taskmanager.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TeamDetailViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun TestScope.createVm(
        repo: FakeTeamRepository,
        teamId: String = "t1",
    ): TeamDetailViewModel {
        val savedState = SavedStateHandle(mapOf("teamId" to teamId))
        val vm = TeamDetailViewModel(savedState, repo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.state.collect {  }
        }
        return vm
    }

    @Test
    fun loadsTeamDetails_combinesTeamProjectsAndMembers() = runTest {
        val team1 = TeamWithCounts(id = "t1", name = "Nhóm DOFPK", role = TeamRole.OWNER, memberCount = 1)
        val project1 = ProjectWithCount(id = "p1", name = "Sprint 1", taskCount = 2)
        val userAn = UserEntity(id = "u1", name = "Nguyễn An", email = "an@deno.com", passwordHash = "")
        val memberAn = MemberWithUser(TeamMemberEntity(teamId = "t1", userId = "u1", role = TeamRole.OWNER), userAn)

        val repo = FakeTeamRepository(
            initialTeams = listOf(team1),
            initialProjects = mapOf("t1" to listOf(project1)),
            initialMembers = mapOf("t1" to listOf(memberAn)),
        )
        val vm = createVm(repo, teamId = "t1")

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertEquals("Nhóm DOFPK", state.teamName)
        assertEquals(1, state.projects.size)
        assertEquals("Sprint 1", state.projects[0].name)
        assertEquals(1, state.members.size)
        assertEquals("Nguyễn An", state.members[0].user.name)
    }

    @Test
    fun createProject_success_addsProjectToTeam() = runTest {
        val repo = FakeTeamRepository(
            initialTeams = listOf(TeamWithCounts(id = "t1", name = "Nhóm DOFPK", role = TeamRole.OWNER, memberCount = 1)),
        )
        val vm = createVm(repo, teamId = "t1")

        vm.createProject("Sprint 2")

        val state = vm.state.value
        assertEquals(1, state.projects.size)
        assertEquals("Sprint 2", state.projects[0].name)
    }

    @Test
    fun  addMember_success_addMemberToTeam() = runTest {
        val repo = FakeTeamRepository(
            initialTeams = listOf(TeamWithCounts(id = "t1", name = "Nhóm DOFPK", role = TeamRole.OWNER, memberCount = 1))
        )

        val vm = createVm(repo, teamId = "t1")

        vm.addMember("binh@demo.com")

        val state = vm.state.value
        assertEquals(1, state.members.size)
        assertEquals("binh@demo.com", state.members[0].user.email)
    }
}
