package com.example.taskmanager.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.taskmanager.core.database.entity.CommentEntity
import com.example.taskmanager.core.database.entity.UserEntity

/**
 * Data class đại diện cho một bình luận kèm thông tin tác giả.
 *
 * @property comment Đối tượng bình luận ([CommentEntity]).
 * @property author Đối tượng người dùng ([UserEntity]) là tác giả của bình luận.
 */
data class CommentWithAuthor(
    @Embedded val comment: CommentEntity,
    @Relation(parentColumn = "authorId", entityColumn = "id")
    val author: UserEntity,
)