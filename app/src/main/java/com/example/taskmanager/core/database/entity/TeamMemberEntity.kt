package com.example.taskmanager.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.taskmanager.core.model.TeamRole

/**
 * Đại diện cho bảng liên kết `team_members` trong DB Room, lưu trữ quan hệ nhiều-nhiều
 * giữa các thành viên và team
 *
 * Khóa chính của bảng là cặp giá trị kết hợp từ [teamId] và [userId].
 *
 * Các setup quan trọng bao gồm:
 * - Khóa ngoại liên kết tới table `TeamEntity` (thông qua [teamId]) và `UserEntity` (thông qua [userId]),
 *   với việc tự động xóa ([ForeignKey.CASCADE]) khi record cha bị xóa.
 * - Chỉ mục ([Index]) được tạo trên cột [userId] để tối ưu hóa hiệu suất truy vấn.
 *
 * @property teamId indentity của team, tham chiếu đến [TeamEntity.id].
 * @property userId identity của users, tham chiếu đến [UserEntity.id].
 * @property role Vai trò của thành viên trong team, mặc định là [TeamRole.MEMBER].
 */

@Entity(
    tableName = "team_members",
    primaryKeys = ["teamId", "userId"],
    foreignKeys = [
        ForeignKey(TeamEntity::class, ["id"], ["teamId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(UserEntity::class, ["id"], ["userId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("userId")],
)
data class TeamMemberEntity(
    val teamId: String,
    val userId: String,
    val role: TeamRole = TeamRole.MEMBER,
)
