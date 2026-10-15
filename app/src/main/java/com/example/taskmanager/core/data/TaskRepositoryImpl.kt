package com.example.taskmanager.core.data

import com.example.taskmanager.core.database.dao.TaskDao
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(private val dao: TaskDao) : TaskRepository {
    override fun observeByStatus(projectId: String, status: TaskStatus) = TODO()
    override fun observeCounts(projectId: String) = TODO()
    override fun observeMine() = TODO()
    override fun observe(taskId: String) = TODO()
    override suspend fun create(projectId: String, title: String, description: String?,
                                priority: Priority, dueDate: Long?, assigneeId: String?) = TODO()
    override suspend fun update(task: TaskEntity) = TODO()
    override suspend fun updateStatus(id: String, status: TaskStatus) = TODO()
    override suspend fun assign(id: String, userId: String?) = TODO()
    override suspend fun delete(id: String) = TODO()
}
