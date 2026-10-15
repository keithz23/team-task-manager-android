package com.example.taskmanager.core.data

import com.example.taskmanager.core.database.dao.StatusCount
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeByStatus(projectId: String, status: TaskStatus): Flow<List<TaskWithAssignee>>
    fun observeCounts(projectId: String): Flow<List<StatusCount>>
    fun observeMine(): Flow<List<TaskWithAssignee>>
    fun observe(taskId: String): Flow<TaskWithAssignee?>
    suspend fun create(
        projectId: String, title: String, description: String?,
        priority: Priority, dueDate: Long?, assigneeId: String?
    ): Result<Unit>

    suspend fun update(task: TaskEntity): Result<Unit>
    suspend fun updateStatus(id: String, status: TaskStatus)
    suspend fun assign(id: String, userId: String?)
    suspend fun delete(id: String)
}


