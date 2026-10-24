package com.example.taskmanager.core.data

import androidx.room.Query
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
import kotlinx.coroutines.flow.emptyFlow

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
            when (s) {
                is AuthState.LoggedIn -> teamDao.observeTeamsOf(s.userId)
                // Chưa đọc xong phiên đăng nhập: chưa phát gì, để màn vẫn hiện "đang tải"
                // thay vì hiện thoáng "Chưa có nhóm nào".
                AuthState.Loading -> emptyFlow()
                AuthState.LoggedOut -> flowOf(emptyList())
            }
        }

    override fun observeMembers(teamId: String) = teamDao.observeMembers(teamId)

    override fun observeProjects(teamId: String) = projectDao.observeByTeam(teamId)

    override fun observeTeamCount(userId: String): Flow<Int> =
        teamDao.observeTeamCount(userId)


    override suspend fun createTeam(
        name: String,
        firstProject: String?
    ): Result<String> = runCatching {
        val normalizedName = name.trim()
        require(normalizedName.isNotEmpty()) {
            "Tên nhóm không được để trống"
        }

        val userId = requireUserId()

        db.withTransaction {
            check(!teamDao.existsByName(normalizedName)) {
                "Tên nhóm đã tồn tại"
            }

            val team = TeamEntity(name = normalizedName)
            teamDao.createTeam(
                team,
                ownerId = userId
            )

            firstProject
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let { projectName ->
                    projectDao.insert(
                        ProjectEntity(teamId = team.id, name = projectName)
                    )
                }

            team.id
        }
    }

    override suspend fun createProject(teamId: String, name: String): Result<Unit> =
        runCatching {
            val normalizedName = name.trim()
            require(normalizedName.isNotBlank()) { "Tên project không được để trống" }
            check(!projectDao.existsByTeamAndName(teamId, normalizedName)) {
                "Tên project đã tồn tại"
            }
            val role = teamDao.roleOf(teamId, requireUserId()) ?: error("Bạn không thuộc nhóm này")
            check(role != TeamRole.MEMBER) { "Chỉ Owner/Admin mới tạo được project" }
            projectDao.insert(ProjectEntity(teamId = teamId, name = name.trim()))
        }

    override suspend fun addMemberByEmail(teamId: String, email: String): Result<Unit> =
        runCatching {
            val myRole =
                teamDao.roleOf(teamId, requireUserId()) ?: error("Bạn không thuộc nhóm này")
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

    // Repository implementation
    override suspend fun existsByTeamAndName(
        teamId: String,
        name: String
    ): Result<Boolean> = runCatching {
        projectDao.existsByTeamAndName(teamId, name)
    }
}
