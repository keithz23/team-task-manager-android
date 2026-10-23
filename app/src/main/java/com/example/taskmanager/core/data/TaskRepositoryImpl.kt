package com.example.taskmanager.core.data

import com.example.taskmanager.core.database.dao.TaskDao
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.session.AuthState
import com.example.taskmanager.core.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao,
    private val session: SessionManager,
) : TaskRepository {

    override fun observeByStatus(projectId: String, status: TaskStatus) =
        dao.observeByStatus(projectId, status)

    override fun observeCounts(projectId: String) = dao.observeCounts(projectId)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeMine(): Flow<List<TaskWithAssignee>> =
        session.authState.flatMapLatest { state ->
            if (state is AuthState.LoggedIn) {
                dao.observeMine(state.userId)
            } else {
                flowOf(emptyList())
            }
        }

    override fun observe(taskId: String): Flow<TaskWithAssignee?> = dao.observe(taskId)

    override fun observeCompletedTaskCount(userId: String): Flow<Int> =
        dao.observeCompletedTaskCount(userId)

    //create a new task, the subject is required and not null
    override suspend fun create(
        projectId: String, title: String, description: String?, priority: Priority,
        dueDate: Long?, assigneeId: String?
    ): Result<Unit> = runCatching {
        require(title.isNotBlank()) { "Tiêu đề của dự án không được phép trống!" }
        val task = TaskEntity(
            projectId = projectId,
            title = title.trim(),
            description = description?.trim() ?: "",
            priority = priority,
            dueDate = dueDate,
            assigneeId = assigneeId
        )
        dao.insert(task)
    }

    override suspend fun update(task: TaskEntity): Result<Unit> = runCatching {
        dao.update(task)
    }

    override suspend fun updateStatus(id: String, status: TaskStatus) {
        dao.updateStatus(id, status)
    }

    override suspend fun assign(id: String, userId: String?) {
        dao.assign(id, userId)
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }
}
