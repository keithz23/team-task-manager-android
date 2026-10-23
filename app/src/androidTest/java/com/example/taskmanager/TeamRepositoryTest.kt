package com.example.taskmanager

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.core.data.TeamRepository
import com.example.taskmanager.core.database.SeedData
import com.example.taskmanager.core.model.TeamRole
import com.example.taskmanager.core.session.SessionManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TeamRepositoryTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var repo: TeamRepository
    @Inject lateinit var session: SessionManager

    @Before
    fun setUp() = hiltRule.inject()

    @Test
    fun observeTeams_whenLoggedIn_returnsTeams() = runTest {
        session.save(SeedData.USER_AN)

        val teams = repo.observeTeams().first()

        assertEquals(1, teams.size)
        assertEquals(SeedData.TEAM_ID, teams[0].id)
        assertEquals(TeamRole.OWNER, teams[0].role)
    }

    @Test
    fun createTeam_withFirstProject_insertsTeamAndProjectAndOwner() = runTest {
        session.save(SeedData.USER_AN)

        val result = repo.createTeam("Nhóm DOFPK", "Sprint Alpha")

        assertTrue(result.isSuccess)
        val teamId = result.getOrThrow()

        val projects = repo.observeProjects(teamId).first()
        assertEquals(1, projects.size)
        assertEquals("Sprint Alpha", projects[0].name)

        val members = repo.observeMembers(teamId).first()
        assertEquals(1, members.size)
        assertEquals(SeedData.USER_AN, members[0].user.id)
        assertEquals(TeamRole.OWNER, members[0].member.role)
    }

    @Test
    fun createTeam_blankName_fails() = runTest {
        session.save(SeedData.USER_AN)

        val result = repo.createTeam("   ", null)

        assertTrue(result.isFailure)
        assertEquals("Tên nhóm không được để trống", result.exceptionOrNull()?.message)
    }

    @Test
    fun createProject_asOwner_addsProjectToTeam() = runTest {
        session.save(SeedData.USER_AN)

        val result = repo.createProject(SeedData.TEAM_ID, "Sprint 2")

        assertTrue(result.isSuccess)
        val projects = repo.observeProjects(SeedData.TEAM_ID).first()
        assertTrue(projects.any { it.name == "Sprint 2" })
    }

    @Test
    fun addMemberByEmail_userExists_insertsMember() = runTest {
        session.save(SeedData.USER_AN)

        val teamId = repo.createTeam("Team Thêm Thành Viên", null).getOrThrow()

        val addResult = repo.addMemberByEmail(teamId, SeedData.EMAIL_BINH)

        assertTrue(addResult.isSuccess)
        val members = repo.observeMembers(teamId).first()
        assertEquals(2, members.size)
        assertTrue(members.any { it.user.email == SeedData.EMAIL_BINH })
    }

    @Test
    fun findUserByEmail_returnsTrueWhenExists_falseOtherwise() = runTest {
        assertTrue(repo.findUserByEmail(SeedData.EMAIL_AN).getOrThrow())
        assertTrue(repo.findUserByEmail(SeedData.EMAIL_BINH).getOrThrow())
        assertFalse(repo.findUserByEmail("nonexistent@demo.com").getOrThrow())
    }
}
