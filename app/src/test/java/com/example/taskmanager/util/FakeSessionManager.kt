package com.example.taskmanager.util

import com.example.taskmanager.core.session.AuthState
import com.example.taskmanager.core.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Session giả lưu trong bộ nhớ. Truyền userId để giả lập đã đăng nhập. */
class FakeSessionManager(initialUserId: String? = null) : SessionManager {
    private val userId = MutableStateFlow(initialUserId)

    override val authState: Flow<AuthState> =
        userId.map { id -> if (id == null) AuthState.LoggedOut else AuthState.LoggedIn(id) }

    override suspend fun currentUserId(): String? = userId.value
    override suspend fun save(userId: String) { this.userId.value = userId }
    override suspend fun clear() { userId.value = null }
}