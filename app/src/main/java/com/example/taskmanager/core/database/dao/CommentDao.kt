package com.example.taskmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.taskmanager.core.database.entity.CommentEntity
import com.example.taskmanager.core.database.relation.CommentWithAuthor
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) cung cấp các phương thức thao tác với bảng `comments` trong cơ sở dữ liệu Room.
 */
@Dao
interface CommentDao {

    /**
     * Thêm mới một bình luận vào cơ sở dữ liệu.
     *
     * @param c Đối tượng [CommentEntity] cần thêm mới.
     */
    @Insert
    suspend fun insert(c: CommentEntity)

    /**
     * Lắng nghe danh sách các bình luận thuộc một task cụ thể theo thời gian thực,
     * kèm theo thông tin chi tiết của tác giả, được sắp xếp theo thời gian tạo tăng dần (từ cũ đến mới).
     *
     * @param taskId Identity của task cần lấy danh sách bình luận.
     * @return Một [Flow] phát ra danh sách [CommentWithAuthor] chứa thông tin bình luận và tác giả.
     */
    @Transaction
    @Query("SELECT * FROM comments WHERE taskId = :taskId ORDER BY createdAt")
    fun observe(taskId: String): Flow<List<CommentWithAuthor>>
}
