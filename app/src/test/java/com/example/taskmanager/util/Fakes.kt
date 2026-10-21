package com.example.taskmanager.util

import com.example.taskmanager.core.data.CommentRepository
import com.example.taskmanager.core.database.entity.CommentEntity
import com.example.taskmanager.core.database.entity.UserEntity
import com.example.taskmanager.core.database.relation.CommentWithAuthor
import com.example.taskmanager.feature.auth.AuthRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
        val newUser = UserEntity(id = "u-${users.size + 1}", name = name, email = email, passwordHash = password)
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
