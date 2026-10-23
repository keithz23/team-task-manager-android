package com.example.taskmanager.util

import com.example.taskmanager.core.data.TaskRepository
import com.example.taskmanager.core.data.TeamRepository
import com.example.taskmanager.core.database.dao.StatusCount
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.database.relation.MemberWithUser
import com.example.taskmanager.core.database.relation.ProjectWithCount
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.database.relation.TeamWithCounts
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTaskRepository(
    initialTasks: List<TaskWithAssignee> = emptyList()
) : TaskRepository {
    // Danh sách task lưu trên RAM
    val tasks = MutableStateFlow(initialTasks)

    // Ghi lại lịch sử các lần gọi create để assert trong test
    val createdCalls = mutableListOf<TaskEntity>()

    override fun observeByStatus(projectId: String, status: TaskStatus): Flow<List<TaskWithAssignee>> =
        tasks.map { list -> list.filter { it.task.projectId == projectId && it.task.status == status } }

    override fun observeCounts(projectId: String): Flow<List<StatusCount>> =
        tasks.map { list ->
            list.filter { it.task.projectId == projectId }
                .groupBy { it.task.status }
                .map { StatusCount(it.key, it.value.size) }
        }

    override fun observeMine(): Flow<List<TaskWithAssignee>> =
        tasks.map { list -> list.filter { it.task.status != TaskStatus.DONE } }

    override fun observe(taskId: String): Flow<TaskWithAssignee?> =
        tasks.map { list -> list.find { it.task.id == taskId } }

    override suspend fun create(
        projectId: String, title: String, description: String?,
        priority: Priority, dueDate: Long?, assigneeId: String?
    ): Result<Unit> {
        val entity = TaskEntity(
            id = "task-${tasks.value.size + 1}",
            projectId = projectId,
            title = title,
            description = description ?: "",
            priority = priority,
            dueDate = dueDate,
            assigneeId = assigneeId
        )
        createdCalls += entity
        tasks.update { it + TaskWithAssignee(entity, null) }
        return Result.success(Unit)
    }

    override suspend fun update(task: TaskEntity): Result<Unit> {
        tasks.update { list -> list.map { if (it.task.id == task.id) it.copy(task = task) else it } }
        return Result.success(Unit)
    }

    override suspend fun updateStatus(id: String, status: TaskStatus) {
        tasks.update { list -> list.map { if (it.task.id == id) it.copy(task = it.task.copy(status = status)) else it } }
    }

    override suspend fun assign(id: String, userId: String?) {
        tasks.update { list -> list.map { if (it.task.id == id) it.copy(task = it.task.copy(assigneeId = userId)) else it } }
    }

    override suspend fun delete(id: String) {
        tasks.update { list -> list.filter { it.task.id != id } }
    }
}

class StubTeamRepository(
    private val members: List<MemberWithUser> = emptyList(),
    private val projects: List<ProjectWithCount> = emptyList(),
    private val teams: List<TeamWithCounts> = emptyList()
) : TeamRepository {
    override fun observeMembers(teamId: String): Flow<List<MemberWithUser>> = flowOf(members)
    override fun observeProjects(teamId: String): Flow<List<ProjectWithCount>> = flowOf(projects)
    override fun observeTeams(): Flow<List<TeamWithCounts>> = flowOf(teams)
    override suspend fun createTeam(name: String, firstProject: String?) = Result.success("t-1")
    override suspend fun createProject(teamId: String, name: String) = Result.success(Unit)
    override suspend fun addMemberByEmail(teamId: String, email: String) = Result.success(Unit)
}