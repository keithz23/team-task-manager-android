package com.example.taskmanager.util

import com.example.taskmanager.core.data.CommentRepository
import com.example.taskmanager.core.database.entity.CommentEntity
import com.example.taskmanager.core.database.entity.UserEntity
import com.example.taskmanager.core.database.relation.CommentWithAuthor
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
