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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.ui.theme.tagColors
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale

val TaskStatus.label: String
    get() = when (this) {
        TaskStatus.TODO -> "Cần làm"
        TaskStatus.DOING -> "Đang làm"
        TaskStatus.DONE -> "Hoàn thành"
    }

/**
 * Task quá hạn: có hạn, chưa hoàn thành, và ngày hạn trước hôm nay (hạn hôm nay chưa tính là quá hạn).
 * dueDate do DatePicker lưu là 00:00 UTC của ngày được chọn, nên đổi về ngày theo UTC trước khi so.
 */
fun isOverdue(dueDate: Long?, status: TaskStatus, today: LocalDate = LocalDate.now()): Boolean {
    if (dueDate == null || status == TaskStatus.DONE) return false
    val dueDay = Instant.ofEpochMilli(dueDate).atZone(ZoneOffset.UTC).toLocalDate()
    return dueDay.isBefore(today)
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

            // Phần hạn chuyển màu đỏ và thêm "Quá hạn" khi task quá hạn.
            val overdue = isOverdue(task.dueDate, task.status)
            val errorColor = MaterialTheme.colorScheme.error
            val subtitleText = buildAnnotatedString {
                if (showProject && item.project != null) {
                    append(item.project.name)
                }
                if (task.dueDate != null) {
                    if (length > 0) append(" · ")
                    if (overdue) {
                        withStyle(SpanStyle(color = errorColor, fontWeight = FontWeight.Medium)) {
                            append("Hạn ${formatDue(task.dueDate)} · Quá hạn")
                        }
                    } else {
                        append("Hạn ${formatDue(task.dueDate)}")
                    }
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
