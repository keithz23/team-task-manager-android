package com.example.taskmanager.feature.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.core.data.TaskRepository
import com.example.taskmanager.core.data.TeamRepository
import com.example.taskmanager.core.database.entity.UserEntity
import com.example.taskmanager.core.ui.MessageViewModel
import com.example.taskmanager.feature.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val teamRepository: TeamRepository,
    private val taskRepository: TaskRepository
) : MessageViewModel() {

    val currentUserId: StateFlow<String?> = authRepository.currentUserId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    var user by mutableStateOf<UserEntity?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set

    fun getUserById(userId: String) {
        if (userId.isBlank() || isLoading) return
        viewModelScope.launch {
            isLoading = true
            authRepository.getUser(userId)
                .onSuccess { user = it }
                .onFailure { send(it.message ?: "Đã có lỗi xảy ra") }
            isLoading = false
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val teamCount: StateFlow<Int> = authRepository.currentUserId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(0)
            else teamRepository.observeTeamCount(id)
                .catchWithMessage(0, "Không tải được số nhóm")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val completedTaskCount: StateFlow<Int> = authRepository.currentUserId
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(0)
            else taskRepository.observeCompletedTaskCount(id)
                .catchWithMessage(0, "Không tải được số task")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)


    private fun <T> Flow<T>.catchWithMessage(default: T, fallback: String): Flow<T> =
        catch { e ->
            emit(default)
            send(e.message ?: fallback)
        }
}