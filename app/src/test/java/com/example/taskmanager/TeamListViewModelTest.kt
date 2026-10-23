package com.example.taskmanager

import com.example.taskmanager.core.database.relation.ProjectWithCount
import com.example.taskmanager.core.database.relation.TeamWithCounts
import com.example.taskmanager.core.model.TeamRole
import com.example.taskmanager.feature.team.TeamListViewModel
import com.example.taskmanager.util.FakeTeamRepository
import com.example.taskmanager.util.MainDispatcherRule
import junit.framework.Assert.assertFalse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TeamListViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun  TestScope.createVm(repo: FakeTeamRepository): TeamListViewModel{
        val vm = TeamListViewModel(repo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            vm.state.collect{  }
        }
        return vm
        }

    @Test
    fun emptyTeams_showsEmptyListAndNotLoading() = runTest {
        val repo = FakeTeamRepository(initialTeams = emptyList())
        val vm = createVm(repo)

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertTrue(state.teams.isEmpty())
        assertTrue(state.projectsByTeam.isEmpty())
    }

    @Test
    fun loadsTeams_withTheirProjects() = runTest {
        val team1= TeamWithCounts(id = "t1", name = "Nhóm DOFPK", role = TeamRole.OWNER, memberCount = 2)
        val project1 = ProjectWithCount(id = "p1", name = "Sprint 1", taskCount = 3)
        val project2 = ProjectWithCount(id = "p2", name = "Sprint 2", taskCount = 0)

        val repo = FakeTeamRepository(
            initialTeams = listOf(team1),
            initialProjects = mapOf("t1" to listOf(project1,project2)),
        )
        val vm = createVm(repo)

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertEquals("Nhóm DOFPK", state.teams[0].name)
        val projects = state.projectsByTeam["t1"]
        assertEquals(listOf("Sprint 1","Sprint 2"), projects?.map { it.name})
    }

    @Test
    fun createProject_success_addsProjectToTeam() = runTest {
        val team1= TeamWithCounts(id = "t1", name = "Nhóm DOFPK", role = TeamRole.OWNER, memberCount = 2)
        val  repo = FakeTeamRepository(initialTeams = listOf(team1))
        val  vm = createVm(repo)

        vm.createProject(teamId = "t1", name = "Project mới")

        val projects = vm.state.value.projectsByTeam["t1"]
        assertEquals(1,projects?.size)
        assertEquals("Project mới", projects?.first()?.name)
    }
}