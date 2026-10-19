package com.example.taskmanager.core.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus

// Token màu của app. Màn hình không dùng trực tiếp các giá trị này mà dùng
// MaterialTheme.colorScheme.xxx (đã map trong Theme.kt) hoặc tagColors() bên dưới.
val Blue600 = Color(0xFF2447C8)
val Blue50 = Color(0xFFE8EDFC)
val Blue900 = Color(0xFF0F2167)
val Gray50 = Color(0xFFF6F7F9)
val Gray100 = Color(0xFFE4E7EE)
val Gray300 = Color(0xFFCDD2DC)
val Gray600 = Color(0xFF4A5160)
val Gray900 = Color(0xFF14181F)
val Red700 = Color(0xFFB42318)
val Red50 = Color(0xFFFEF3F2)
val Amber700 = Color(0xFFB54708)
val Amber50 = Color(0xFFFFFAEB)
val Green700 = Color(0xFF027A48)
val Green50 = Color(0xFFECFDF3)

/** Cặp màu nền + chữ cho chip/nhãn. */
data class TagColors(val container: Color, val content: Color)

fun Priority.tagColors(): TagColors = when (this) {
    Priority.LOW -> TagColors(Green50, Green700)
    Priority.MEDIUM -> TagColors(Amber50, Amber700)
    Priority.HIGH -> TagColors(Red50, Red700)
}

fun TaskStatus.tagColors(): TagColors = when (this) {
    TaskStatus.TODO -> TagColors(Gray100, Gray600)
    TaskStatus.DOING -> TagColors(Blue50, Blue600)
    TaskStatus.DONE -> TagColors(Green50, Green700)
}
