package com.example.taskmanager.feature.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.core.data.TeamRepository
import com.example.taskmanager.core.database.relation.ProjectWithCount
import com.example.taskmanager.core.database.relation.TeamWithCounts
import com.example.taskmanager.core.ui.MessageViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamListUiState(
    val isLoading: Boolean = true,
    val teams: List<TeamWithCounts> = emptyList(),
    val projectsByTeam: Map<String, List<ProjectWithCount>> = emptyMap(),
    val error: String? = null,
)

@HiltViewModel
class TeamListViewModel @Inject constructor(private val repo: TeamRepository) : MessageViewModel() {
    val state: StateFlow<TeamListUiState> = repo.observeTeams()
        .flatMapLatest { teams ->
            if (teams.isEmpty()) {
                flowOf(TeamListUiState(isLoading = false, teams = emptyList()))
            }else{
                combine(
                    teams.map { team ->
                        repo.observeProjects(team.id).map { projects -> team.id to projects }
                    }
                ){ pairs->
                    TeamListUiState(
                        isLoading = false,
                        teams = teams,
                        projectsByTeam = pairs.toMap(),
                    )
                    }
            }
        }
        .catch { emit(TeamListUiState(isLoading = false, error = it.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TeamListUiState())
    fun createProject(teamId: String, name: String){
        viewModelScope.launch {
            repo.createProject(teamId,name).onFailure {
                send(it.message?: "Không thể tạo project")
            }
        }
    }
}
