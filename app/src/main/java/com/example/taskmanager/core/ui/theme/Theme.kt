package com.example.taskmanager.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Blue600, onPrimary = Color.White,
    primaryContainer = Blue50, onPrimaryContainer = Blue900,
    background = Gray50, onBackground = Gray900,
    surface = Color.White, onSurface = Gray900,
    surfaceVariant = Gray50, onSurfaceVariant = Gray600,
    outline = Gray300, outlineVariant = Gray100,
    error = Red700, onError = Color.White,
    errorContainer = Red50, onErrorContainer = Red700,
)

private val Dark = darkColorScheme(
    primary = DarkBlue, onPrimary = Color(0xFF0B1E5C),
    primaryContainer = DarkBlueContainer, onPrimaryContainer = Color(0xFFDCE4FB),
    background = DarkBg, onBackground = DarkOnSurface,
    surface = DarkSurface, onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant, onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline, outlineVariant = DarkOutlineVariant,
    error = DarkRed, onError = Color(0xFF601410),
    errorContainer = DarkRedContainer, onErrorContainer = Color(0xFFFFDAD6),
)

/** Cờ chế độ tối, để tagColors() và các component tự chọn màu. */
val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) Dark else Light,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}