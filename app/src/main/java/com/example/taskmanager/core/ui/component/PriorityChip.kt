package com.example.taskmanager.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.ui.theme.tagColors

val Priority.label: String
    get() = when (this) {
        Priority.LOW -> "Thấp"
        Priority.MEDIUM -> "Trung bình"
        Priority.HIGH -> "Cao"
    }

/**
 * Chip mức ưu tiên.
 * - Chỉ hiển thị (TaskCard): PriorityChip(task.priority)
 * - Chọn trong form: PriorityChip(p, selected = p == state.priority, onClick = { onPriority(p) })
 */
@Composable
fun PriorityChip(
    priority: Priority,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = priority.tagColors()
    val border = if (selected) BorderStroke(1.5.dp, colors.content) else null
    val content: @Composable () -> Unit = {
        Text(
            text = priority.label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
    if (onClick == null) {
        Surface(modifier, MaterialTheme.shapes.small, colors.container, colors.content, border = border, content = content)
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier,
            selected = selected,
            shape = MaterialTheme.shapes.small,
            color = colors.container,
            contentColor = colors.content,
            border = border,
            content = content,
        )
    }
}
