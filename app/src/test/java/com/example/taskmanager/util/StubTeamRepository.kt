package com.example.taskmanager.util

import com.example.taskmanager.core.data.TeamRepository
import com.example.taskmanager.core.database.relation.MemberWithUser
import com.example.taskmanager.core.database.relation.ProjectWithCount
import com.example.taskmanager.core.database.relation.TeamWithCounts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class StubTeamRepository(
    private val members: List<MemberWithUser> = emptyList(),
    private val projects: List<ProjectWithCount> = emptyList(),
    private val teams: List<TeamWithCounts> = emptyList()
) : TeamRepository {
    override fun observeMembers(teamId: String): Flow<List<MemberWithUser>> = flowOf(members)
    override fun observeTeamCount(userId: String): Flow<Int> = flowOf(1)

    override fun observeProjects(teamId: String): Flow<List<ProjectWithCount>> = flowOf(projects)
    override fun observeTeams(): Flow<List<TeamWithCounts>> = flowOf(teams)
    override suspend fun createTeam(name: String, firstProject: String?) = Result.success("t-1")
    override suspend fun createProject(teamId: String, name: String) = Result.success(Unit)
    override suspend fun addMemberByEmail(teamId: String, email: String) = Result.success(Unit)
    override suspend fun findUserByEmail(email: String): Result<Boolean> = Result.success(true)

    override suspend fun existsByTeamAndName(
        teamId: String,
        name: String
    ): Result<Boolean> = Result.success(false)
}
