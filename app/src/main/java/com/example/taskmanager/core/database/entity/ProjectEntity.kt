package com.example.taskmanager.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.taskmanager.core.model.TeamRole
import com.example.taskmanager.core.model.TeamRole.MEMBER
import java.util.UUID

/**
 * Đại diện cho bảng `projects` trong DB Room, lưu trữ thông tin về các dự án.
 *
 * Khóa chính của bảng là [id], được tự động tạo giá trị ngẫu nhiên dưới dạng UUID.
 *
 * Các thiết lập quan trọng bao gồm:
 * - Khóa ngoại liên kết tới bảng `TeamEntity` (thông qua [teamId]),
 *   với việc tự động xóa ([ForeignKey.CASCADE]) khi record cha bị xóa.
 * - Chỉ mục ([Index]) được tạo trên cột [teamId] để tối ưu hóa hiệu suất truy vấn theo nhóm.
 *
 * @property id unique identity của dự án, đóng vai trò là khóa chính.
 * @property teamId identity của nhóm sở hữu dự án, tham chiếu đến [TeamEntity.id].
 * @property name Tên của dự án.
 */

@Entity(
    tableName = "projects",
    foreignKeys = [ForeignKey(TeamEntity::class, ["id"], ["teamId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("teamId")],
)
data class ProjectEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val teamId: String,
    val name: String,
)

