package com.example.taskmanager.core.data

import com.example.taskmanager.core.database.relation.CommentWithAuthor
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun observe(taskId: String): Flow<List<CommentWithAuthor>>
    suspend fun add(taskId: String, content: String): Result<Unit>
}
