package com.example.taskmanager.util

import com.example.taskmanager.core.data.CommentRepository
import com.example.taskmanager.core.data.TaskRepository
import com.example.taskmanager.core.data.TeamRepository
import com.example.taskmanager.core.database.dao.StatusCount
import com.example.taskmanager.core.database.entity.CommentEntity
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.database.entity.TeamMemberEntity
import com.example.taskmanager.core.database.entity.UserEntity
import com.example.taskmanager.core.database.relation.CommentWithAuthor
import com.example.taskmanager.core.database.relation.MemberWithUser
import com.example.taskmanager.core.database.relation.ProjectWithCount
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.database.relation.TeamWithCounts
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.model.TeamRole
import com.example.taskmanager.feature.auth.AuthRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Tạo UserEntity nhanh cho test. */
fun testUser(id: String, name: String) =
    UserEntity(id = id, name = name, email = "$id@test.com", passwordHash = "")

/**
 * CommentRepository giả trong bộ nhớ. Bình luận mới luôn do [author] viết.
 * - [failNext]: lần add() kế tiếp trả về lỗi này.
 * - [gate]: nếu khác null, add() chờ gate.complete(Unit) mới chạy tiếp (để kiểm tra trạng thái "đang gửi").
 */
class FakeCommentRepository(
    private val author: UserEntity,
    initial: List<CommentWithAuthor> = emptyList(),
) : CommentRepository {
    val comments = MutableStateFlow(initial)
    var failNext: Throwable? = null
    var gate: CompletableDeferred<Unit>? = null
    val added = mutableListOf<Pair<String, String>>()

    override fun observe(taskId: String): Flow<List<CommentWithAuthor>> =
        comments.map { list -> list.filter { it.comment.taskId == taskId } }

    override suspend fun add(taskId: String, content: String): Result<Unit> {
        gate?.await()
        failNext?.let { failNext = null; return Result.failure(it) }
        added += taskId to content
        val entity = CommentEntity(taskId = taskId, authorId = author.id, content = content.trim())
        comments.update { it + CommentWithAuthor(entity, author) }
        return Result.success(Unit)
    }
}

/**
 * AuthRepository giả trong bộ nhớ cho Unit Test AuthViewModel.
 */
class FakeAuthRepository(
    initialUsers: List<UserEntity> = emptyList(),
    initialCurrentUserId: String? = null,
) : AuthRepository {
    val users = mutableMapOf<String, UserEntity>().apply {
        initialUsers.forEach { put(it.id, it) }
    }
    val currentUserIdFlow = MutableStateFlow<String?>(initialCurrentUserId)

    override val currentUserId: Flow<String?> = currentUserIdFlow

    var failLoginMsg: String? = null
    var failRegisterMsg: String? = null
    var failGetUserMsg: String? = null
    var gate: CompletableDeferred<Unit>? = null

    val loginCalls = mutableListOf<Pair<String, String>>()
    val registerCalls = mutableListOf<Triple<String, String, String>>()

    override suspend fun login(email: String, password: String): Result<Unit> {
        gate?.await()
        loginCalls += email to password
        failLoginMsg?.let { return Result.failure(Exception(it)) }
        val user = users.values.firstOrNull { it.email == email }
        return if (user != null) {
            currentUserIdFlow.value = user.id
            Result.success(Unit)
        } else {
            Result.failure(Exception("Email hoặc mật khẩu không đúng"))
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<Unit> {
        gate?.await()
        registerCalls += Triple(name, email, password)
        failRegisterMsg?.let { return Result.failure(Exception(it)) }
        if (users.values.any { it.email == email }) {
            return Result.failure(Exception("Email đã được sử dụng"))
        }
        val newUser = UserEntity(
            id = "u-${users.size + 1}",
            name = name,
            email = email,
            passwordHash = password
        )
        users[newUser.id] = newUser
        currentUserIdFlow.value = newUser.id
        return Result.success(Unit)
    }

    override suspend fun getUser(userId: String): Result<UserEntity> {
        failGetUserMsg?.let { return Result.failure(Exception(it)) }
        val user = users[userId]
        return if (user != null) {
            Result.success(user)
        } else {
            Result.failure(Exception("Không tìm thấy người dùng"))
        }
    }

    override suspend fun logout() {
        currentUserIdFlow.value = null
    }
}

/**
 * TeamRepository giả cho Unit Test.
 */
class FakeTeamRepository(
    initialTeams: List<TeamWithCounts> = emptyList(),
    initialProjects: Map<String, List<ProjectWithCount>> = emptyMap(),
    initialMembers: Map<String, List<MemberWithUser>> = emptyMap(),
) : TeamRepository {
    val teamCountFlow = MutableStateFlow(initialTeams.size)
    var teamCountError: Throwable? = null

    val teamsFlow = MutableStateFlow(initialTeams)
    val projectsFlow = MutableStateFlow(initialProjects)
    val membersFlow = MutableStateFlow(initialMembers)

    override fun observeTeamCount(userId: String): Flow<Int> = kotlinx.coroutines.flow.flow {
        teamCountError?.let { throw it }
        teamCountFlow.collect { emit(it) }
    }

    override fun observeTeams(): Flow<List<TeamWithCounts>> =
        teamsFlow

    override fun observeProjects(teamId: String): Flow<List<ProjectWithCount>> =
        projectsFlow.map { it[teamId].orEmpty() }

    override fun observeMembers(teamId: String): Flow<List<MemberWithUser>> =
        membersFlow.map { it[teamId].orEmpty() }

    override suspend fun createTeam(name: String, firstProject: String?): Result<String> =
        Result.success("t-1")

    override suspend fun createProject(teamId: String, name: String): Result<Unit> {
        val current = projectsFlow.value.toMutableMap()
        val list = current[teamId].orEmpty().toMutableList()
        list.add(ProjectWithCount(id = "p-${list.size + 1}", name = name, taskCount = 0))
        current[teamId] = list
        projectsFlow.value = current
        return Result.success(Unit)
    }

    override suspend fun addMemberByEmail(teamId: String, email: String): Result<Unit> {
        val current = membersFlow.value.toMutableMap()
        val list = current[teamId].orEmpty().toMutableList()
        val user =
            UserEntity(id = "u-${list.size + 1}", name = email, email = email, passwordHash = "")
        val member = MemberWithUser(
            member = TeamMemberEntity(teamId = teamId, userId = user.id, role = TeamRole.MEMBER),
            user = user,
        )
        list.add(member)
        current[teamId] = list
        membersFlow.value = current
        return Result.success(Unit)
    }

    override suspend fun findUserByEmail(email: String): Result<Boolean> = Result.success(true)

    override suspend fun existsByTeamAndName(teamId: String, name: String): Result<Boolean> {
        val projects = projectsFlow.value[teamId].orEmpty()
        return Result.success(projects.any { it.name.equals(name, ignoreCase = true) })
    }
}

/**
 * TaskRepository giả cho Unit Test.
 */
class FakeTaskRepository(
    initialTasks: List<TaskWithAssignee> = emptyList()
) : TaskRepository {

    val tasks = MutableStateFlow(initialTasks)

    val createdCalls = mutableListOf<TaskEntity>()

    val completedTaskCountFlow = MutableStateFlow(0)

    var completedTaskCountError: Throwable? = null

    /** Gán để create() trả Result.failure, giả lập lỗi khi tạo task. */
    var createError: Throwable? = null

    /** Gán để delete() ném lỗi, giả lập lỗi khi xoá task. */
    var deleteError: Throwable? = null

    val mineTasksFlow =
        MutableStateFlow<List<TaskWithAssignee>>(emptyList())

    override fun observeCompletedTaskCount(
        userId: String
    ): Flow<Int> = flow {
        completedTaskCountError?.let { throw it }

        completedTaskCountFlow.collect {
            emit(it)
        }
    }

    override fun observeByStatus(
        projectId: String,
        status: TaskStatus
    ): Flow<List<TaskWithAssignee>> {
        return tasks.map { list ->
            list.filter {
                it.task.projectId == projectId &&
                        it.task.status == status
            }
        }
    }

    override fun observeCounts(
        projectId: String
    ): Flow<List<StatusCount>> {
        return tasks.map { list ->
            list.filter { it.task.projectId == projectId }
                .groupingBy { it.task.status }.eachCount()
                .map { (status, count) -> StatusCount(status, count) }
        }
    }

    override fun observeMine(): Flow<List<TaskWithAssignee>> {
        return mineTasksFlow
    }

    override fun observe(
        taskId: String
    ): Flow<TaskWithAssignee?> {
        return tasks.map { list ->
            list.firstOrNull {
                it.task.id == taskId
            }
        }
    }

    override suspend fun create(
        projectId: String,
        title: String,
        description: String?,
        priority: Priority,
        dueDate: Long?,
        assigneeId: String?
    ): Result<Unit> {
        createError?.let { return Result.failure(it) }

        val task = TaskEntity(
            id = "created-${createdCalls.size + 1}",
            projectId = projectId,
            title = title,
            description = String(description?.toCharArray() ?: charArrayOf()),
            status = TaskStatus.TODO,
            priority = priority,
            dueDate = dueDate,
            assigneeId = assigneeId
        )

        createdCalls.add(task)

        tasks.value = tasks.value + TaskWithAssignee(
            task = task,
            assignee = null
        )

        return Result.success(Unit)
    }

    override suspend fun update(
        task: TaskEntity
    ): Result<Unit> {

        tasks.value = tasks.value.map {
            if (it.task.id == task.id) {
                it.copy(task = task)
            } else {
                it
            }
        }

        return Result.success(Unit)
    }

    override suspend fun updateStatus(
        id: String,
        status: TaskStatus
    ) {
        tasks.value = tasks.value.map {
            if (it.task.id == id) {
                it.copy(
                    task = it.task.copy(
                        status = status
                    )
                )
            } else {
                it
            }
        }
    }

    override suspend fun assign(
        id: String,
        userId: String?
    ) {
        tasks.value = tasks.value.map {
            if (it.task.id == id) {
                it.copy(
                    task = it.task.copy(
                        assigneeId = userId
                    )
                )
            } else {
                it
            }
        }
    }

    override suspend fun delete(
        id: String
    ) {
        deleteError?.let { throw it }
        tasks.value = tasks.value.filter {
            it.task.id != id
        }
    }
}