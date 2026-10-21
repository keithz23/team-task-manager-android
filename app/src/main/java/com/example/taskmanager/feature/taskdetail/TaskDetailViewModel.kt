package com.example.taskmanager.feature.taskdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.taskmanager.core.data.TaskRepository
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.navigation.TaskDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repo: TaskRepository,
) : ViewModel() {
    val taskId : String = savedState.toRoute<TaskDetail>().taskId
    val task: StateFlow<TaskWithAssignee?> = repo.observe(taskId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun updateStatus(newStatus: TaskStatus) = viewModelScope.launch {
        repo.updateStatus(taskId, newStatus)
    }
}
