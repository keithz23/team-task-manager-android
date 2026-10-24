package com.example.taskmanager.feature.team

import androidx.lifecycle.viewModelScope
import com.example.taskmanager.core.data.TeamRepository
import com.example.taskmanager.core.ui.MessageViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewTeamUiState(
    val name: String = "",
    val firstProject: String = "",
    val emailInput: String = "",
    val pendingEmails: List<String> = emptyList(),
    val nameError: String? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class NewTeamViewModel @Inject constructor(
    private val repo: TeamRepository,
) : MessageViewModel() {
    private val _state = MutableStateFlow(NewTeamUiState())
    val state: StateFlow<NewTeamUiState> = _state.asStateFlow()

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, nameError = null) }
    }

    fun onFirstProjectChange(value: String) {
        _state.update { it.copy(firstProject = value) }
    }

    fun onEmailInputChange(value: String) {
        _state.update { it.copy(emailInput = value) }
    }

    fun addEmail() {
        val email = _state.value.emailInput.trim().lowercase()
        if (email.isBlank()) return

        if (email in _state.value.pendingEmails) {
            send("Email đã có trong danh sách")
            return
        }

        viewModelScope.launch {
            repo.findUserByEmail(email)
                .onSuccess { exists ->
                    if (exists) {
                        _state.update {
                            it.copy(
                                pendingEmails = it.pendingEmails + email,
                                emailInput = "",
                            )
                        }
                    } else{
                        send("Không tìm thấy người dùng với email này")
                    }
                }
                .onFailure { err ->
                    send(err.message ?: "Lỗi kiểm tra email")
                }
        }
    }

    fun createTeam(onDone: () -> Unit) {
        val s = _state.value
        if (s.isLoading) return
        if (s.name.isBlank()) {
            _state.update { it.copy(nameError = "Tên nhóm không được bỏ trống") }

            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, nameError = null) }
            val project = s.firstProject.trim().takeIf { it.isNotEmpty() }

            repo.createTeam(s.name.trim(), project)
                .onSuccess { teamId ->
                    s.pendingEmails.forEach { email ->
                        repo.addMemberByEmail(teamId, email)
                            .onFailure { send(it.message ?: "Không thêm được $email") }
                    }
                    _state.update { it.copy(isLoading = false) }
                    onDone()
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false) }
                    send(err.message ?: "Không thể tạo nhóm")
                }
        }
    }

}

