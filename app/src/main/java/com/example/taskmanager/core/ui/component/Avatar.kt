package com.example.taskmanager.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.taskmanager.core.database.entity.UserEntity
import com.example.taskmanager.core.ui.theme.DarkAvatarPalette
import com.example.taskmanager.core.ui.theme.LightAvatarPalette
import com.example.taskmanager.core.ui.theme.LocalDarkTheme
import kotlin.math.absoluteValue

/** "Nguyễn Văn An" → "VA" (chữ đầu của 2 từ cuối). Tên rỗng → "?". */
fun initialsOf(name: String): String =
    name.trim().split(Regex("\\s+"))
        .takeLast(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
        .ifEmpty { "?" }

/**
 * Avatar tròn hiển thị chữ cái đầu. Màu cố định theo tên (cùng tên luôn cùng màu).
 * Dùng trong TaskCard, danh sách thành viên, bình luận: AvatarInitials(user.name).
 */
@Composable
fun AvatarInitials(name: String, modifier: Modifier = Modifier, size: Dp = 32.dp) {
    val dark = LocalDarkTheme.current
    val palette = if (dark) DarkAvatarPalette else LightAvatarPalette
    val colors = palette[name.hashCode().absoluteValue % palette.size]
    val fontSize = with(LocalDensity.current) { (size * 0.4f).toSp() }
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(colors.container),
        contentAlignment = Alignment.Center,
    ) {
        Text(initialsOf(name), color = colors.content, fontSize = fontSize, fontWeight = FontWeight.Bold)
    }
}

/** Avatar lớn cho màn Profile. */
@Composable
fun Avatar(user: UserEntity?) {
    AvatarInitials(name = user?.name.orEmpty(), size = 80.dp)
}