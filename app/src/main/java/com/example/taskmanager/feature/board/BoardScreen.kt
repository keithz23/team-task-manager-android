package com.example.taskmanager.feature.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskmanager.core.ui.StubScreen

@Composable
fun BoardScreen(
    projectId: String,
    teamId: String,
    onTask: (String) -> Unit,
    onBack: () -> Unit,
    vm: BoardViewModel = hiltViewModel(),
    ) {
    val taskList by vm.tasks.collectAsStateWithLifecycle()
    var textInput by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Dự án: $projectId", style = MaterialTheme.typography.titleMedium)
        // Ô nhập tiêu đề
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Nhập tiêu đề task mới") },
            modifier = Modifier.fillMaxWidth()
        )
        // Nút bấm thêm
        Button(
            onClick = {
                if (textInput.isNotBlank()) {
                    vm.createTask(textInput) // Gọi Controller xử lý
                    textInput = ""           // Xóa trắng ô nhập sau khi tạo
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Thêm công việc")
        }
        Divider()
        Text("Danh sách việc cần làm (${taskList.size}):", style = MaterialTheme.typography.titleSmall)
        // Danh sách hiển thị các task (giống ListView)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(taskList, key = { it.task.id }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item.task.title,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
