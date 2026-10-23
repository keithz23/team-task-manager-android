package com.example.taskmanager.core.ui.theme

import androidx.compose.runtime.Composable
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

// Chỉ dùng cho avatar (thêm màu để ít người bị trùng màu).
val Purple700 = Color(0xFF6941C6)
val Purple50 = Color(0xFFF4F3FF)
val Teal700 = Color(0xFF107569)
val Teal50 = Color(0xFFE6F7F5)
val Pink700 = Color(0xFFC11574)
val Pink50 = Color(0xFFFDF2FA)
val Indigo700 = Color(0xFF3538CD)
val Indigo50 = Color(0xFFEEF4FF)

// Dark tokens
val DarkBg = Color(0xFF0F1218)
val DarkSurface = Color(0xFF171B23)
val DarkSurfaceVariant = Color(0xFF1E232C)
val DarkOutline = Color(0xFF3A4150)
val DarkOutlineVariant = Color(0xFF2A303B)
val DarkOnSurface = Color(0xFFE6E8EE)
val DarkOnSurfaceVariant = Color(0xFFA6ADBB)
val DarkBlue = Color(0xFF9DB4FF)
val DarkBlueContainer = Color(0xFF1B2A63)
val DarkRed = Color(0xFFFF8A80)
val DarkRedContainer = Color(0xFF5C1A14)
val DarkAmber = Color(0xFFFFD866)
val DarkAmberContainer = Color(0xFF4D3A00)
val DarkGreen = Color(0xFF8FDCB0)
val DarkGreenContainer = Color(0xFF0F3D26)
val DarkPurple = Color(0xFFD0BCFF)
val DarkPurpleContainer = Color(0xFF381E72)
val DarkTeal = Color(0xFF80CBC4)
val DarkTealContainer = Color(0xFF004D40)
val DarkPink = Color(0xFFFFB1C8)
val DarkPinkContainer = Color(0xFF630B38)
val DarkIndigo = Color(0xFFB3C5FF)
val DarkIndigoContainer = Color(0xFF232599)

val LightAvatarPalette = listOf(
    TagColors(Blue50, Blue600),
    TagColors(Green50, Green700),
    TagColors(Amber50, Amber700),
    TagColors(Red50, Red700),
    TagColors(Purple50, Purple700),
    TagColors(Teal50, Teal700),
    TagColors(Pink50, Pink700),
    TagColors(Indigo50, Indigo700),
)

val DarkAvatarPalette = listOf(
    TagColors(DarkBlueContainer, DarkBlue),
    TagColors(DarkGreenContainer, DarkGreen),
    TagColors(DarkAmberContainer, DarkAmber),
    TagColors(DarkRedContainer, DarkRed),
    TagColors(DarkPurpleContainer, DarkPurple),
    TagColors(DarkTealContainer, DarkTeal),
    TagColors(DarkPinkContainer, DarkPink),
    TagColors(DarkIndigoContainer, DarkIndigo),
)

/** Cặp màu nền + chữ cho chip/nhãn. */
data class TagColors(val container: Color, val content: Color)

@Composable
fun Priority.tagColors(): TagColors {
    val dark = LocalDarkTheme.current
    return when (this) {
        Priority.LOW -> if (dark) TagColors(DarkGreenContainer, DarkGreen) else TagColors(Green50, Green700)
        Priority.MEDIUM -> if (dark) TagColors(DarkAmberContainer, DarkAmber) else TagColors(Amber50, Amber700)
        Priority.HIGH -> if (dark) TagColors(DarkRedContainer, DarkRed) else TagColors(Red50, Red700)
    }
}

@Composable
fun TaskStatus.tagColors(): TagColors {
    val dark = LocalDarkTheme.current
    return when (this) {
        TaskStatus.TODO -> if (dark) TagColors(DarkOutlineVariant, DarkOnSurfaceVariant) else TagColors(Gray100, Gray600)
        TaskStatus.DOING -> if (dark) TagColors(DarkBlueContainer, DarkBlue) else TagColors(Blue50, Blue600)
        TaskStatus.DONE -> if (dark) TagColors(DarkGreenContainer, DarkGreen) else TagColors(Green50, Green700)
    }
}