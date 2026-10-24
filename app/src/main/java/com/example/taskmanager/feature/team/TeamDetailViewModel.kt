package com.example.taskmanager.feature.team

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.taskmanager.core.data.TeamRepository
import com.example.taskmanager.core.database.relation.MemberWithUser
import com.example.taskmanager.core.database.relation.ProjectWithCount
import com.example.taskmanager.core.ui.MessageViewModel
import com.example.taskmanager.navigation.TeamDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.taskmanager.core.model.TeamRole

data class TeamDetailUiState(
    val isLoading: Boolean = true,
    val teamName: String = "",
    val projects: List<ProjectWithCount> = emptyList(),
    val members: List<MemberWithUser> = emptyList(),
    val error: String? = null,
    /** Chủ nhóm/Quản trị mới được thêm project và thành viên. */
    val canManage: Boolean = false,
)

@HiltViewModel
class TeamDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: TeamRepository,
) : MessageViewModel() {
    private val teamId: String = savedStateHandle.get<String>("teamId") ?: runCatching { savedStateHandle.toRoute<TeamDetail>().teamId }.getOrDefault("")

    val state: StateFlow<TeamDetailUiState> = combine(
        repo.observeTeams(),
        repo.observeProjects(teamId),
        repo.observeMembers(teamId),
    ) { teams, projects, members ->
        val team = teams.find { it.id == teamId }
        TeamDetailUiState(
            isLoading = false,
            teamName = team?.name ?: "Chi tiết nhóm",
            projects = projects,
            members = members,
            canManage = team != null && team.role != TeamRole.MEMBER,
        )
    }.catch { err ->
        emit(TeamDetailUiState(isLoading = false, error = err.message))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TeamDetailUiState(),
    )

    fun createProject(name: String) {
        viewModelScope.launch {
            repo.createProject(teamId, name)
                .onSuccess { send("Đã tạo project mới") }
                .onFailure { send(it.message ?: "Không thể tạo project") }
        }
    }

    fun addMember(email: String) {
        viewModelScope.launch {
            repo.addMemberByEmail(teamId, email)
                .onSuccess { send("Đã thêm thành viên vào nhóm") }
                .onFailure { send(it.message ?: "Không thể thêm thành viên") }
        }
    }


}
