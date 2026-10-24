package com.example.taskmanager

import com.example.taskmanager.core.util.toRelativeTime
import org.junit.Assert.assertEquals
import org.junit.Test

class RelativeTimeTest {

    private val now = 1_700_000_000_000L
    private val minute = 60_000L
    private val hour = 60 * minute
    private val day = 24 * hour

    @Test
    fun underOneMinute_isJustNow() {
        assertEquals("Vừa xong", now.toRelativeTime(now))
        assertEquals("Vừa xong", (now - 59_000L).toRelativeTime(now))
    }

    @Test
    fun minutesHoursDays() {
        assertEquals("5 phút trước", (now - 5 * minute).toRelativeTime(now))
        assertEquals("3 giờ trước", (now - 3 * hour).toRelativeTime(now))
        assertEquals("2 ngày trước", (now - 2 * day).toRelativeTime(now))
    }

    @Test
    fun futureTime_isJustNow() {
        assertEquals("Vừa xong", (now + minute).toRelativeTime(now))
    }

    @Test
    fun overSevenDays_showsDate() {
        val text = (now - 10 * day).toRelativeTime(now)
        assertEquals(true, Regex("""\d{2}/\d{2}/\d{4}""").matches(text))
    }
}
