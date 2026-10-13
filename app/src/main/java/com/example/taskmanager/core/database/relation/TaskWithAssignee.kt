package com.example.taskmanager.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.database.entity.UserEntity

/**
 * Data class đại diện cho một công việc kèm theo thông tin chi tiết của người được phân công (Assignee).
 * Sử dụng quan hệ một-một (hoặc một-nhiều) thông qua Room `@Relation`.
 *
 * @property task Đối tượng công việc ([TaskEntity]).
 * @property assignee Đối tượng người dùng ([UserEntity]) được phân công thực hiện công việc đó (có thể là `null` nếu chưa phân công cho ai).
 */

data class TaskWithAssignee(
    @Embedded val task: TaskEntity,
    @Relation(parentColumn = "assigneeId", entityColumn = "id")
    val assignee: UserEntity?,
)