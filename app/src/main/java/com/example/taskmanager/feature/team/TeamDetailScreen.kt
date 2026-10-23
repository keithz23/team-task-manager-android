package com.example.taskmanager.feature.team

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskmanager.core.ui.CollectMessages
import com.example.taskmanager.core.ui.StubScreen
import com.example.taskmanager.core.ui.component.AvatarInitials
import androidx.compose.ui.unit.dp
import com.example.taskmanager.core.database.relation.MemberWithUser
import com.example.taskmanager.core.database.relation.ProjectWithCount
import com.example.taskmanager.core.ui.theme.AppTheme
import com.example.taskmanager.core.database.entity.TeamMemberEntity
import com.example.taskmanager.core.database.entity.UserEntity
import com.example.taskmanager.core.model.TeamRole
import com.example.taskmanager.core.ui.component.label

@Composable
fun TeamDetailScreen(
    teamId: String,
    onProject: (String) -> Unit,
    onBack: () -> Unit,
    vm: TeamDetailViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    CollectMessages(vm.messages)

    TeamDetailContent(
        state = state,
        onProjectClick = onProject,
        onCreateProject = vm::createProject,
        onAddMember = vm::addMember,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailContent(
    state: TeamDetailUiState,
    onProjectClick: (String) -> Unit,
    onCreateProject: (String) -> Unit,
    onAddMember: (String) -> Unit,
    onBack: () -> Unit,
) {
    var showProjectDialog by remember { mutableStateOf(false) }
    var showMemberDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nhóm của tôi",
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = state.teamName.ifEmpty { "Chi tiết nhóm" },
                style = MaterialTheme.typography.headlineMedium,
            )

            if (state.isLoading) {
                CircularProgressIndicator()
                return@Column
            }

            state.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Project", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "+ Thêm",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showProjectDialog = true })
                    }

                    if (state.projects.isEmpty()) {
                        HorizontalDivider()
                        Text(
                            text = "Chưa có project nào",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    } else {
                        state.projects.forEach { project ->
                            HorizontalDivider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onProjectClick(project.id) }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(project.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "${project.taskCount} task",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Thành viên (${state.members.size})",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "+ Thêm",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showMemberDialog = true },
                        )
                    }

                    state.members.forEach { item ->
                        HorizontalDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            AvatarInitials(name = item.user.name, size = 40.dp)
                            Text(
                                text = item.user.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = item.member.role.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        if (showProjectDialog) {
            var name by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showProjectDialog = false },
                title = { Text("Thêm project mới") },
                text = {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Tên project...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                onCreateProject(name.trim())
                                showProjectDialog = false
                            }
                        }) {
                        Text("Thêm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showProjectDialog = false }) {
                        Text("Hủy")
                    }
                },
            )
        }
        if (showMemberDialog) {
            var email by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showMemberDialog = false },
                title = { Text(" Thêm thành viên mới") },
                text = {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email thành viên...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (email.isNotBlank()) {
                                onAddMember(email.trim())
                                showMemberDialog = false
                            }
                        }
                    ) {
                        Text("Thêm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMemberDialog = false }) {
                        Text("Hủy")
                    }
                },
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamDetailPreview() {
    AppTheme {
        TeamDetailContent(
            state = TeamDetailUiState(
                isLoading = false,
                teamName = "Nhóm Đồ án Android",
                projects = listOf(ProjectWithCount("p1", "Sprint đồ án", 9)),
                members = listOf(
                    MemberWithUser(
                        TeamMemberEntity("t1", "u1", TeamRole.OWNER),
                        UserEntity("u1", "An (bạn)", "an@demo.com", ""),
                    ),
                ),
            ),
            onProjectClick = {},
            onCreateProject = {},
            onAddMember = {},
            onBack = {},
        )
    }
}
