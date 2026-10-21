package com.example.taskmanager.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

/**
 * Thời gian tương đối so với [now]: "Vừa xong", "5 phút trước", "3 giờ trước", "2 ngày trước".
 * Quá 7 ngày thì hiện ngày "dd/MM/yyyy". Dùng cho giờ bình luận.
 */
fun Long.toRelativeTime(now: Long = System.currentTimeMillis()): String {
    val diff = now - this
    return when {
        diff < MINUTE -> "Vừa xong"
        diff < HOUR -> "${diff / MINUTE} phút trước"
        diff < DAY -> "${diff / HOUR} giờ trước"
        diff < 7 * DAY -> "${diff / DAY} ngày trước"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(this))
    }
}
