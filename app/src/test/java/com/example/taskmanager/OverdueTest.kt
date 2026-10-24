package com.example.taskmanager

import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.ui.component.isOverdue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class OverdueTest {
    private val today = LocalDate.of(2026, 10, 24)

    /** Giống cách DatePicker lưu: 00:00 UTC của ngày được chọn. */
    private fun utcMidnight(date: LocalDate) = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    @Test fun yesterday_notDone_isOverdue() =
        assertTrue(isOverdue(utcMidnight(today.minusDays(1)), TaskStatus.TODO, today))

    @Test fun today_isNotOverdue() =
        assertFalse(isOverdue(utcMidnight(today), TaskStatus.DOING, today))

    @Test fun tomorrow_isNotOverdue() =
        assertFalse(isOverdue(utcMidnight(today.plusDays(1)), TaskStatus.TODO, today))

    @Test fun pastButDone_isNotOverdue() =
        assertFalse(isOverdue(utcMidnight(today.minusDays(5)), TaskStatus.DONE, today))

    @Test fun noDueDate_isNotOverdue() =
        assertFalse(isOverdue(null, TaskStatus.TODO, today))
}
