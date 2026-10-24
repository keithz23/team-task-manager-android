package com.example.taskmanager.core.ui.component

import com.example.taskmanager.core.model.TeamRole

/**
 * Tên vai trò hiển thị cho người dùng. Dùng thay cho role.name ("OWNER", "MEMBER"…):
 * Text(member.role.label)
 */
val TeamRole.label: String
    get() = when (this) {
        TeamRole.OWNER -> "Chủ nhóm"
        TeamRole.ADMIN -> "Quản trị"
        TeamRole.MEMBER -> "Thành viên"
    }
