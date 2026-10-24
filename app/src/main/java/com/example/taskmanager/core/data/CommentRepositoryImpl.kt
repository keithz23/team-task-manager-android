package com.example.taskmanager.core.data

import com.example.taskmanager.core.database.dao.CommentDao
import com.example.taskmanager.core.database.entity.CommentEntity
import com.example.taskmanager.core.database.relation.CommentWithAuthor
import com.example.taskmanager.core.session.SessionManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val dao: CommentDao,
    private val session: SessionManager,
) : CommentRepository {

    override fun observe(taskId: String): Flow<List<CommentWithAuthor>> = dao.observe(taskId)

    /** Tác giả là user đang đăng nhập. Nội dung được trim, không được rỗng, tối đa [MAX_LENGTH] ký tự. */
    override suspend fun add(taskId: String, content: String): Result<Unit> =
        runCatching {
            val text = content.trim()
            require(text.isNotEmpty()) { "Bình luận không được để trống" }
            require(text.length <= MAX_LENGTH) { "Bình luận tối đa $MAX_LENGTH ký tự" }
            val userId = session.currentUserId() ?: error("Chưa đăng nhập")
            dao.insert(CommentEntity(taskId = taskId, authorId = userId, content = text))
        }

    companion object {
        const val MAX_LENGTH = 1000
    }
}
