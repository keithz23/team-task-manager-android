package com.example.taskmanager.feature.board

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.taskmanager.core.data.TaskRepository
import com.example.taskmanager.core.data.TeamRepository
import com.example.taskmanager.core.database.relation.MemberWithUser
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.navigation.Board
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repo: TaskRepository,
    private val repoTeam: TeamRepository,
) : ViewModel() {
    val projectId: String = savedState.toRoute<Board>().projectId
    val teamId: String = savedState.toRoute<Board>().teamId
    private val _selectedStatus = MutableStateFlow(TaskStatus.TODO)
    val selectedStatus: StateFlow<TaskStatus> = _selectedStatus.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<TaskWithAssignee>> = selectedStatus
        .flatMapLatest {status -> repo.observeByStatus(projectId, status)}
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    val members: StateFlow<List<MemberWithUser>> = repoTeam.observeMembers(teamId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    val projectName: StateFlow<String> = repoTeam.observeProjects(teamId)
        .map { list -> list.find { it.id == projectId }?.name ?: "Bảng" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Loading....")

    fun selectStatus(status: TaskStatus) {
        _selectedStatus.value = status
    }

    fun createTask(title: String, description: String?, priority: Priority, dueDate: Long?, assigneeId: String?) {
        viewModelScope.launch {
            repo.create(
                projectId = projectId,
                title = title,
                description = description,
                priority = priority,
                dueDate = dueDate,
                assigneeId = assigneeId
            )
        }
    }
}
