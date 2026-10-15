package com.example.taskmanager.feature.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.core.data.TeamRepository
import com.example.taskmanager.core.database.relation.TeamWithCounts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TeamListUiState(
    val isLoading: Boolean = true,
    val teams: List<TeamWithCounts> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class TeamListViewModel @Inject constructor(repo: TeamRepository) : ViewModel() {
    val state: StateFlow<TeamListUiState> = repo.observeTeams()
        .map { TeamListUiState(isLoading = false, teams = it) }
        .catch { emit(TeamListUiState(isLoading = false, error = it.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TeamListUiState())
}
