package com.example.taskmanager.feature.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.taskmanager.core.ui.component.label
import com.example.taskmanager.core.ui.CollectMessages
import androidx.compose.foundation.layout.Row

@OptIn(ExperimentalMaterial3Api::class)
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
    val selectedStatus by vm.selectedStatus.collectAsStateWithLifecycle()
    val counts by vm.counts.collectAsStateWithLifecycle()
    val tabs = listOf(TaskStatus.TODO, TaskStatus.DOING, TaskStatus.DONE)
    CollectMessages(vm.messages)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = projectName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = { showSheet = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Tạo Task mới")
            }

            TabRow(
                selectedTabIndex = tabs.indexOf(selectedStatus),
                modifier = Modifier.fillMaxWidth(),
            ) {
                tabs.forEach { status ->
                    val tabTitle = status.label
                    val isSelected = selectedStatus == status

                    Tab(
                        selected = isSelected,
                        onClick = { vm.selectStatus(status) },
                        text = {
                            val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            val weight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            // Tên tab và số lượng là 2 Text riêng (UI test vẫn tìm được đúng "Cần làm");
                            // số lượng nằm dòng dưới để "Hoàn thành" không bị xuống dòng.
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = tabTitle, fontWeight = weight, color = color, maxLines = 1)
                                Text(
                                    text = "${counts[status] ?: 0} task",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color,
                                )
                            }
                        },
                    )
                }
            }

            if (taskList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Không có công việc nào",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(taskList, key = { it.task.id }) { item ->
                        TaskCard(
                            item = item,
                            onClick = { onTask(item.task.id) },
                        )
                    }
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
                    assigneeId = assigneeId,
                )
            },
        )
    }
}
