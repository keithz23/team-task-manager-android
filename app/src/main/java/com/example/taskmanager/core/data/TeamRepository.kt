package com.example.taskmanager.core.data

import com.example.taskmanager.core.database.relation.MemberWithUser
import com.example.taskmanager.core.database.relation.ProjectWithCount
import com.example.taskmanager.core.database.relation.TeamWithCounts
import kotlinx.coroutines.flow.Flow

interface TeamRepository {
    fun observeTeams(): Flow<List<TeamWithCounts>>
    fun observeProjects(teamId: String): Flow<List<ProjectWithCount>>
    fun observeMembers(teamId: String): Flow<List<MemberWithUser>>
    suspend fun createTeam(name: String, firstProject: String?): Result<String>
    suspend fun createProject(teamId: String, name: String): Result<Unit>
    suspend fun addMemberByEmail(teamId: String, email: String): Result<Unit>
    suspend fun findUserByEmail(email: String): Result<Boolean>
}

