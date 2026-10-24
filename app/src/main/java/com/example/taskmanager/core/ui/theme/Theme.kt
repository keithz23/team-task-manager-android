package com.example.taskmanager.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Light = lightColorScheme(
    primary = Blue600, onPrimary = Color.White,
    primaryContainer = Blue50, onPrimaryContainer = Blue900,
    secondary = SecondaryLight, onSecondary = Color.White,
    secondaryContainer = SecondaryContainerLight, onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = Teal700, onTertiary = Color.White,
    tertiaryContainer = TertiaryContainerLight, onTertiaryContainer = OnTertiaryContainerLight,
    background = Gray50, onBackground = Gray900,
    surface = Color.White, onSurface = Gray900,
    surfaceVariant = Gray50, onSurfaceVariant = Gray600,
    outline = Gray300, outlineVariant = Gray100,
    error = Red700, onError = Color.White,
    errorContainer = Red50, onErrorContainer = Red700,
    inverseSurface = InverseSurfaceLight, inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = DarkBlue,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = Gray100,
)

private val Dark = darkColorScheme(
    primary = DarkBlue, onPrimary = Color(0xFF0B1E5C),
    primaryContainer = DarkBlueContainer, onPrimaryContainer = Color(0xFFDCE4FB),
    secondary = SecondaryDark, onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark, onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = DarkTeal, onTertiary = OnTertiaryDark,
    tertiaryContainer = DarkTealContainer, onTertiaryContainer = OnTertiaryContainerDark,
    background = DarkBg, onBackground = DarkOnSurface,
    surface = DarkSurface, onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant, onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline, outlineVariant = DarkOutlineVariant,
    error = DarkRed, onError = Color(0xFF601410),
    errorContainer = DarkRedContainer, onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = DarkOnSurface, inverseOnSurface = InverseSurfaceLight,
    inversePrimary = Blue600,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = SurfaceContainerHighestDark,
)

/** Cờ chế độ tối, để tagColors() và các component tự chọn màu. */
val LocalDarkTheme = staticCompositionLocalOf { false }

/**
 * Theme của app.
 * @param dynamicColor true = trên Android 12+ lấy màu theo hình nền máy (Material You) thay cho
 * bảng màu riêng. Mặc định TẮT để giữ màu thương hiệu. Màu Priority/Status/avatar (tagColors)
 * vẫn giữ nguyên dù bật, vì chúng mang ý nghĩa (đỏ = Cao…).
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> Dark
        else -> Light
    }
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}