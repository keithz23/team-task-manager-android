package com.example.taskmanager.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Đại diện cho bảng `comments` trong DB Room, lưu trữ thông tin về các bình luận trong task.
 *
 * Khóa chính của bảng là [id], được tự động tạo giá trị ngẫu nhiên dưới dạng UUID.
 *
 * Các thiết lập quan trọng bao gồm:
 * - Khóa ngoại liên kết tới bảng `TaskEntity` (thông qua [taskId]), với việc tự động xóa ([ForeignKey.CASCADE]) khi task cha bị xóa.
 * - Khóa ngoại liên kết tới bảng `UserEntity` (thông qua [authorId]), với việc tự động xóa ([ForeignKey.CASCADE]) khi người viết bình luận bị xóa.
 * - Các chỉ mục ([Index]) được tạo trên cột [taskId] và [authorId] để tối ưu hóa hiệu suất truy vấn.
 *
 * @property id Unique identity của bình luận, đóng vai trò là khóa chính.
 * @property taskId Identity của task chứa bình luận này, tham chiếu đến [TaskEntity.id].
 * @property authorId Identity của người dùng viết bình luận, tham chiếu đến [UserEntity.id].
 * @property content Nội dung của bình luận.
 * @property createdAt Thời điểm bình luận được tạo.
 */

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(TaskEntity::class, ["id"], ["taskId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(UserEntity::class, ["id"], ["authorId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("taskId"), Index("authorId")],
)
data class CommentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val authorId: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
)
