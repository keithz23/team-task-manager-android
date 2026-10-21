package com.example.taskmanager.feature.taskdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskmanager.feature.comment.CommentSection

@Composable
fun TaskDetailScreen(
    onBack: () -> Unit,
    vm: TaskDetailViewModel = hiltViewModel()
) {
    val taskWithAssignee by vm.task.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { /* Nút Back, Title... */ }
    ) { padding ->
        taskWithAssignee?.let { item ->
            Column(modifier = Modifier.padding(padding)) {
                // 1. Hiển thị thông tin Task (Title, Description, Status, Priority, Due date...)
                Text(
                    text = item.task.title,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                CommentSection(taskId = vm.taskId)
            }
        } ?: run {
            // Loading hoặc không tìm thấy task
        }
    }
}
