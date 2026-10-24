package com.example.taskmanager

import com.example.taskmanager.core.model.TeamRole
import com.example.taskmanager.core.ui.component.label
import org.junit.Assert.assertEquals
import org.junit.Test

class TeamRoleLabelTest {
    @Test fun labels() {
        assertEquals("Chủ nhóm", TeamRole.OWNER.label)
        assertEquals("Quản trị", TeamRole.ADMIN.label)
        assertEquals("Thành viên", TeamRole.MEMBER.label)
    }
}
