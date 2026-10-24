package com.example.taskmanager.util

import com.example.taskmanager.core.session.AuthState
import com.example.taskmanager.core.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Session giả cho androidTest, thay DataStoreSessionManager qua TestModules.
 * Mặc định LoggedOut. Inject vào test rồi gọi save(SeedData.USER_AN) để giả lập đã đăng nhập.
 */
@Singleton
class FakeSessionManager @Inject constructor() : SessionManager {
    private val userId = MutableStateFlow<String?>(null)

    override val authState: Flow<AuthState> =
        userId.map { id -> if (id == null) AuthState.LoggedOut else AuthState.LoggedIn(id) }

    override suspend fun currentUserId(): String? = userId.value
    override suspend fun save(userId: String) { this.userId.value = userId }
    override suspend fun clear() { userId.value = null }
}
