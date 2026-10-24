package com.example.taskmanager.core.database.relation

import com.example.taskmanager.core.model.TeamRole

/**
 * Data class đại diện cho thông tin tóm tắt của một nhóm ([Team]), bao gồm số lượng thành viên
 * và vai trò của người dùng hiện tại trong nhóm đó (thường dùng cho kết quả truy vấn kết hợp và thống kê).
 *
 * @property id Unique identity của nhóm.
 * @property name Tên của nhóm.
 * @property role Vai trò của người dùng trong nhóm (kiểu [TeamRole]).
 * @property memberCount Tổng số thành viên hiện có trong nhóm.
 */

data class TeamWithCounts(
    val id: String,
    val name: String,
    val role: TeamRole,
    val memberCount: Int,
)
