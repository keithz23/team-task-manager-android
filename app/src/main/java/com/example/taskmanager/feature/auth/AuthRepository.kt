package com.example.taskmanager.feature.auth

import com.example.taskmanager.core.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: Flow<String?>
    suspend fun register(name: String, email: String, password: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun getUser(userId:String): Result<UserEntity>
    suspend fun logout()
}
