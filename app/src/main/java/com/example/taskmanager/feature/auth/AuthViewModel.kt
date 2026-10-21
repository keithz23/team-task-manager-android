package com.example.taskmanager.feature.auth

import com.example.taskmanager.core.database.entity.UserEntity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.core.ui.MessageViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : MessageViewModel() {

    // Login
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")

    // Register
    var registerFullName by mutableStateOf("")
    var registerEmail by mutableStateOf("")
    var registerPassword by mutableStateOf("")
    var registerConfirmPassword by mutableStateOf("")

    // Trạng thái chung
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
    var error by mutableStateOf<String?>(null)
        private set

    var emailError by mutableStateOf<String?>(null)
        private set
    var passwordError by mutableStateOf<String?>(null)
        private set
    var fullNameError by mutableStateOf<String?>(null)
        private set
    var confirmPasswordError by mutableStateOf<String?>(null)
        private set


    private fun isValidEmail(email: String): Boolean {
        return EMAIL_REGEX.matches(email)
    }

    fun login() {
        emailError = null
        passwordError = null

        var hasError = false

        // Validate Email
        if (loginEmail.isBlank()) {
            emailError = "Email không được để trống"
            hasError = true
        } else if (!isValidEmail(loginEmail)) {
            emailError = "Định dạng email không hợp lệ"
            hasError = true
        }

        // Validate Password
        if (loginPassword.isBlank()) {
            passwordError = "Mật khẩu không được để trống"
            hasError = true
        } else if (loginPassword.length < 6) {
            passwordError = "Mật khẩu phải có ít nhất 6 ký tự"
            hasError = true
        }
        if (!hasError) {
            submit { authRepository.login(loginEmail, loginPassword) }
        }
    }

    fun register() {
        fullNameError = null
        emailError = null
        passwordError = null
        confirmPasswordError = null

        var hasError = false

        if (registerFullName.isBlank()) {
            fullNameError = "Họ tên không được để trống"
            hasError = true
        }

        if (registerEmail.isBlank()) {
            emailError = "Email không được để trống"
            hasError = true
        } else if (!isValidEmail(registerEmail)) {
            emailError = "Định dạng email không hợp lệ"
            hasError = true
        }

        if (registerPassword.isBlank()) {
            passwordError = "Mật khẩu không được để trống"
            hasError = true
        } else if (registerPassword.length < 6) {
            passwordError = "Mật khẩu phải có ít nhất 6 ký tự"
            hasError = true
        }

        if (registerConfirmPassword.isBlank()) {
            confirmPasswordError = "Vui lòng xác nhận lại mật khẩu"
            hasError = true
        } else if (registerPassword != registerConfirmPassword) {
            confirmPasswordError = "Mật khẩu nhập lại không khớp"
            hasError = true
        }

        if (!hasError) {
            submit {
                authRepository.register(registerFullName, registerEmail, registerPassword)
            }
        }
    }

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

    fun clearError() {
        error = null
    }

    private fun submit(call: suspend () -> Result<Unit>) {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            call().onFailure { send(it.message ?: "Đã có lỗi xảy ra") }   // ← send thay vì error =
            isLoading = false
            // Thành công: session đã đổi, AppNavHost tự chuyển sang Teams
        }
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}


