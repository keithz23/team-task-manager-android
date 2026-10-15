package com.example.taskmanager.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Color(0xFF2447C8),
    onPrimary = Color.White,
    background = Color(0xFFF6F7F9),
    surface = Color.White,
    onSurface = Color(0xFF14181F),
    onSurfaceVariant = Color(0xFF4A5160),
    outline = Color(0xFFCDD2DC),
    outlineVariant = Color(0xFFE4E7EE),
    error = Color(0xFFB42318),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Light, content = content)
}

