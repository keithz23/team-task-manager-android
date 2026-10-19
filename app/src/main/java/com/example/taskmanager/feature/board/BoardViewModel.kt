package com.example.taskmanager.feature.board

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.taskmanager.core.data.TaskRepository
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.session.AuthState
import com.example.taskmanager.navigation.Board
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repo: TaskRepository,
) : ViewModel() {
    val projectId: String = savedState.toRoute<Board>().projectId
    val tasks: StateFlow<List<TaskWithAssignee>> = repo.observeByStatus(projectId, TaskStatus.TODO).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun createTask(title: String) {
        viewModelScope.launch {
            repo.create(
                projectId = projectId,
                title = title,
                description = null,
                priority = Priority.MEDIUM,
                dueDate = null,
                assigneeId = null
            )
        }
    }
}
