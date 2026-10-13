package com.example.taskmanager.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.taskmanager.core.database.entity.TeamMemberEntity
import com.example.taskmanager.core.database.entity.UserEntity
import com.example.taskmanager.core.model.TeamRole

/**
 * Data class chứa thông tin thành viên nhóm và thông tin người dùng tương ứng.
 *
 * @property member Thông tin thành viên nhóm ([TeamMemberEntity]).
 * @property user Thông tin người dùng ([UserEntity]) tương ứng với thành viên đó.
 */

data class MemberWithUser(
    @Embedded val member: TeamMemberEntity,
    @Relation(parentColumn = "userId", entityColumn = "id")
    val user: UserEntity,
)
