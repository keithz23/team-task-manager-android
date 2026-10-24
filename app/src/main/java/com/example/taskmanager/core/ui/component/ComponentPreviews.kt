package com.example.taskmanager.core.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.taskmanager.core.database.entity.ProjectEntity
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.database.entity.UserEntity
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.ui.theme.AppTheme

// Xem trước các component chung ở chế độ Sáng và Tối.
// Trong Android Studio mở file này, chọn Split/Design để xem hai bản cạnh nhau.

private val previewTask = TaskWithAssignee(
    task = TaskEntity(
        id = "t1", projectId = "p1", title = "Thiết kế màn hình đăng nhập",
        priority = Priority.HIGH, status = TaskStatus.DOING, dueDate = 1_761_350_400_000,
    ),
    assignee = UserEntity(id = "u1", name = "Nguyễn An", email = "", passwordHash = ""),
    project = ProjectEntity(id = "p1", teamId = "team", name = "App quản lý công việc"),
)

private val previewOverdueTask = TaskWithAssignee(
    task = TaskEntity(
        id = "t2", projectId = "p1", title = "Viết báo cáo tuần",
        priority = Priority.MEDIUM, status = TaskStatus.TODO, dueDate = 1_700_000_000_000,
    ),
    assignee = UserEntity(id = "u2", name = "Trần Bình", email = "", passwordHash = ""),
)

@Composable
private fun ComponentGallery() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { PriorityChip(it) }
                PriorityChip(Priority.MEDIUM, selected = true, onClick = {})
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Nguyễn An", "Trần Bình", "Lê Kiệt", "Phạm Huê").forEach { AvatarInitials(it) }
            }
            TaskCard(previewTask, onClick = {}, showStatus = true, showProject = true)
            TaskCard(previewOverdueTask, onClick = {}, showStatus = true)
            AppTextField("an@demo.com", {}, label = "Email")
            AppTextField("123", {}, label = "Mật khẩu", error = "Mật khẩu tối thiểu 6 ký tự", isPassword = true)
            PrimaryButton("Đăng nhập", {})
            PrimaryButton("Đăng nhập", {}, loading = true)
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun ComponentGalleryLightPreview() {
    AppTheme(darkTheme = false) { ComponentGallery() }
}

@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ComponentGalleryDarkPreview() {
    AppTheme(darkTheme = true) { ComponentGallery() }
}
