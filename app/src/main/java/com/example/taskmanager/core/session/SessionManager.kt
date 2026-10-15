package com.example.taskmanager.core.session

import kotlinx.coroutines.flow.Flow

sealed interface AuthState {
    data object Loading : AuthState
    data object LoggedOut : AuthState
    data class LoggedIn(val userId: String) : AuthState
}

interface SessionManager {
    val authState: Flow<AuthState>
    suspend fun currentUserId(): String?
    suspend fun save(userId: String)
    suspend fun clear()
}
