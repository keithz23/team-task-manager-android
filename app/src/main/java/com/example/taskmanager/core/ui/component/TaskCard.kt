package com.example.taskmanager.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.ui.theme.tagColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val TaskStatus.label: String
    get() = when (this) {
        TaskStatus.TODO -> "Cần làm"
        TaskStatus.DOING -> "Đang làm"
        TaskStatus.DONE -> "Hoàn thành"
    }

/**
 * Thẻ task cho Board và My tasks: tiêu đề, độ ưu tiên, hạn chót, người được giao.
 * [showStatus] = true để hiện thêm nhãn trạng thái.
 * [showProject] = true để hiện thêm tên dự án.
 * Gắn testTag qua modifier nếu UI test cần.
 */
@Composable
fun TaskCard(
    item: TaskWithAssignee,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showStatus: Boolean = false,
    showProject: Boolean = false,
) {
    val task = item.task
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(8.dp))
                PriorityChip(task.priority)
            }

            val subtitleText = buildString {
                if (showProject && item.project != null) {
                    append(item.project.name)
                }
                if (task.dueDate != null) {
                    if (isNotEmpty()) append(" · ")
                    append("Hạn ${formatDue(task.dueDate)}")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (subtitleText.isNotEmpty()) {
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (showStatus) {
                    if (subtitleText.isNotEmpty()) Spacer(Modifier.width(8.dp))
                    StatusLabel(task.status)
                }
                Spacer(Modifier.weight(1f))
                item.assignee?.let { AvatarInitials(it.name, size = 28.dp) }
            }
        }
    }
}

@Composable
private fun StatusLabel(status: TaskStatus) {
    val colors = status.tagColors()
    Surface(shape = MaterialTheme.shapes.small, color = colors.container, contentColor = colors.content) {
        Text(
            status.label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

private fun formatDue(millis: Long): String =
    SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(millis))
