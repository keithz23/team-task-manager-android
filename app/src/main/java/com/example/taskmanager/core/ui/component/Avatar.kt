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
import com.example.taskmanager.core.ui.theme.Amber50
import com.example.taskmanager.core.ui.theme.Amber700
import com.example.taskmanager.core.ui.theme.Blue50
import com.example.taskmanager.core.ui.theme.Blue600
import com.example.taskmanager.core.ui.theme.Green50
import com.example.taskmanager.core.ui.theme.Green700
import com.example.taskmanager.core.ui.theme.Red50
import com.example.taskmanager.core.ui.theme.Red700
import com.example.taskmanager.core.ui.theme.TagColors
import kotlin.math.absoluteValue

private val AvatarPalette = listOf(
    TagColors(Blue50, Blue600),
    TagColors(Green50, Green700),
    TagColors(Amber50, Amber700),
    TagColors(Red50, Red700),
)

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
    val colors = AvatarPalette[name.hashCode().absoluteValue % AvatarPalette.size]
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
