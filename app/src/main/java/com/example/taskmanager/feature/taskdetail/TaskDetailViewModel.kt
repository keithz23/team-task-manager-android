package com.example.taskmanager.feature.taskdetail

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
import com.example.taskmanager.core.model.TeamRole
import com.example.taskmanager.core.session.AuthState
import com.example.taskmanager.core.session.SessionManager
import com.example.taskmanager.navigation.TaskDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repo: TaskRepository,
    private val teamRepo: TeamRepository,
    private val session: SessionManager
) : ViewModel() {

    val taskId: String = savedState.get<String>("taskId")
        ?: savedState.toRoute<TaskDetail>().taskId

    val task: StateFlow<TaskWithAssignee?> = repo.observe(taskId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
    val currentUserId: StateFlow<String?> = session.authState.map { state ->
        if (state is AuthState.LoggedIn) state.userId else null
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val teamId: Flow<String?> = task.flatMapLatest { currentTask ->
        val projectId = currentTask?.task?.projectId ?: return@flatMapLatest flowOf(null)
        teamRepo.observeTeams().flatMapLatest { myTeams ->
            if (myTeams.isEmpty()) return@flatMapLatest flowOf(null)
            val checks = myTeams.map { team ->
                teamRepo.observeProjects(team.id).map { projects ->
                    if (projects.any { it.id == projectId }) team.id else null
                }
            }
            combine(checks) { results: Array<String?> ->
                results.firstOrNull { it != null }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val members: StateFlow<List<MemberWithUser>> = teamId.flatMapLatest { id ->
        if (id != null) teamRepo.observeMembers(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isOwnerOrAdmin: StateFlow<Boolean> = combine(teamId, teamRepo.observeTeams()) { id, myTeams ->
        val myTeam = myTeams.find { it.id == id }
        myTeam?.role == TeamRole.OWNER || myTeam?.role == TeamRole.ADMIN
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val canEdit: StateFlow<Boolean> = combine(task, currentUserId, isOwnerOrAdmin) { currentTask, userId, isOwner ->
        if (isOwner) return@combine true
        val assigneeId = currentTask?.task?.assigneeId
        assigneeId == null || assigneeId == userId
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val canDelete: StateFlow<Boolean> = isOwnerOrAdmin

    fun updateStatus(newStatus: TaskStatus) = viewModelScope.launch {
        repo.updateStatus(taskId, newStatus)
    }

    fun updateTask(
        title: String,
        description: String?,
        priority: Priority,
        dueDate: Long?,
        assigneeId: String?
    ) = viewModelScope.launch {
        val currentTask = task.value?.task ?: return@launch
        val updated = currentTask.copy(
            title = title,
            description = description?.trim() ?: "",
            priority = priority,
            dueDate = dueDate,
            assigneeId = assigneeId
        )
        repo.update(updated)
    }

    fun deleteTask(onSuccess: () -> Unit) = viewModelScope.launch {
        repo.delete(taskId)
        onSuccess()
    }
}
