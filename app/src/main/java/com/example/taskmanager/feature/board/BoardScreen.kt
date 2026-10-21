package com.example.taskmanager.feature.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.ui.component.TaskCard

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
    val selectedStatus = vm.selectedStatus.collectAsStateWithLifecycle()
    val tabs = listOf(TaskStatus.TODO, TaskStatus.DOING, TaskStatus.DONE)
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
            Text("Tạo Task mới")
        }

        TabRow(
            selectedTabIndex = tabs.indexOf(selectedStatus.value),
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEach { status ->
                val tabTitle = when (status) {
                    TaskStatus.TODO -> "Todo"
                    TaskStatus.DOING -> "Doing"
                    TaskStatus.DONE -> "Done"
                }
                val isSelected = selectedStatus.value == status

                Tab(
                    selected = isSelected,
                    onClick = { vm.selectStatus(status) },
                    text = {
                        Text(
                            text = tabTitle,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Text(
            text = "${
                when (selectedStatus.value) {
                    TaskStatus.TODO -> "Todo"
                    TaskStatus.DOING -> "Doing"
                    TaskStatus.DONE -> "Done"
                }
            } (${taskList.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        if (taskList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không có công việc nào",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(taskList, key = { it.task.id }) { item ->
                    // Sử dụng component TaskCard đã có sẵn của app
                    TaskCard(
                        item = item,
                        onClick = { onTask(item.task.id) }
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
