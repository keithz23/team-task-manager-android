package com.example.taskmanager

import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.taskmanager.feature.board.NotPastSelectableDates
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
class NotPastSelectableDatesTest {
    /** Giống cách DatePicker truyền vào: 00:00 UTC của ngày. */
    private fun utcMidnight(date: LocalDate) = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private val today = LocalDate.now()

    @Test fun today_isSelectable() = assertTrue(NotPastSelectableDates.isSelectableDate(utcMidnight(today)))

    @Test fun tomorrow_isSelectable() = assertTrue(NotPastSelectableDates.isSelectableDate(utcMidnight(today.plusDays(1))))

    @Test fun yesterday_isNotSelectable() = assertFalse(NotPastSelectableDates.isSelectableDate(utcMidnight(today.minusDays(1))))

    @Test fun lastYear_isNotSelectable() = assertFalse(NotPastSelectableDates.isSelectableYear(today.year - 1))
}
