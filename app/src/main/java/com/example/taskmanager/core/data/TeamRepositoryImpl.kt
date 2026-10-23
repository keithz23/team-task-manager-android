package com.example.taskmanager.core.data

import androidx.room.withTransaction
import com.example.taskmanager.core.database.AppDatabase
import com.example.taskmanager.core.database.dao.ProjectDao
import com.example.taskmanager.core.database.dao.TeamDao
import com.example.taskmanager.core.database.dao.UserDao
import com.example.taskmanager.core.database.entity.ProjectEntity
import com.example.taskmanager.core.database.entity.TeamEntity
import com.example.taskmanager.core.database.entity.TeamMemberEntity
import com.example.taskmanager.core.database.relation.TeamWithCounts
import com.example.taskmanager.core.model.TeamRole
import com.example.taskmanager.core.session.AuthState
import com.example.taskmanager.core.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class TeamRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val teamDao: TeamDao,
    private val projectDao: ProjectDao,
    private val userDao: UserDao,
    private val session: SessionManager,
) : TeamRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeTeams(): Flow<List<TeamWithCounts>> =
        session.authState.flatMapLatest { s ->
            if (s is AuthState.LoggedIn) teamDao.observeTeamsOf(s.userId) else flowOf(emptyList())
        }

    override fun observeMembers(teamId: String) = teamDao.observeMembers(teamId)

    override fun observeProjects(teamId: String) = projectDao.observeByTeam(teamId)

    override suspend fun createTeam(name: String, firstProject: String?): Result<String> =
        runCatching {
            require(name.isNotBlank()) { "Tên nhóm không được để trống" }
            val userId = requireUserId()
            val team = TeamEntity(name = name.trim())

            // 1 transaction cho cả team + owner + project đầu tiên
            db.withTransaction {
                teamDao.createTeam(team, ownerId = userId)
                firstProject?.trim()?.takeIf { it.isNotEmpty() }?.let {
                    projectDao.insert(ProjectEntity(teamId = team.id, name = it))
                }
            }
            team.id
        }

    override suspend fun createProject(teamId: String, name: String): Result<Unit> =
        runCatching {
            require(name.isNotBlank()) { "Tên project không được để trống" }
            val role = teamDao.roleOf(teamId, requireUserId()) ?: error("Bạn không thuộc nhóm này")
            check(role != TeamRole.MEMBER) { "Chỉ Owner/Admin mới tạo được project" }
            projectDao.insert(ProjectEntity(teamId = teamId, name = name.trim()))
        }

    override suspend fun addMemberByEmail(teamId: String, email: String): Result<Unit> =
        runCatching {
            val myRole = teamDao.roleOf(teamId, requireUserId()) ?: error("Bạn không thuộc nhóm này")
            check(myRole != TeamRole.MEMBER) { "Chỉ Owner/Admin mới thêm được thành viên" }

            val user = userDao.findByEmail(email.trim().lowercase())
                ?: error("Không tìm thấy người dùng với email này")
            check(teamDao.roleOf(teamId, user.id) == null) { "${user.name} đã ở trong nhóm" }

            teamDao.insertMember(TeamMemberEntity(teamId, user.id, TeamRole.MEMBER))
        }

    override suspend fun findUserByEmail(email: String): Result<Boolean> =
        runCatching {
            userDao.findByEmail(email.trim().lowercase()) != null
        }

    private suspend fun requireUserId(): String =
        session.currentUserId() ?: error("Chưa đăng nhập")
}
