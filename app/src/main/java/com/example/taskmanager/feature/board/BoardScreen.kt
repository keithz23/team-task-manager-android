package com.example.taskmanager.feature.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    val projectName by vm.projectName.collectAsStateWithLifecycle()
    val members by vm.members.collectAsStateWithLifecycle()
    var showSheet by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            //Text("Project Name: $projectName", style = MaterialTheme.typography.titleMedium)
            Text(
                text = projectName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Button(
            onClick = {
                showSheet = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Task")
        }
        Divider()
        Text("Danh sách việc cần làm (${taskList.size}):", style = MaterialTheme.typography.titleSmall)
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
    if (showSheet) {
        TaskFormSheet(
            members = members,
            onDismiss = { showSheet = false },
            onSubmit = { title, desc, priority, dueDate, assigneeId ->
                vm.createTask(
                    title = title,
                    description = desc,
                    priority = priority,
                    dueDate = dueDate,
                    assigneeId = assigneeId
                )
            }
        )
    }
}
