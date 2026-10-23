package com.example.taskmanager.feature.team

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskmanager.core.database.relation.TeamWithCounts
import com.example.taskmanager.core.model.TeamRole
import com.example.taskmanager.core.ui.CollectMessages
import com.example.taskmanager.core.ui.Tags
import com.example.taskmanager.core.ui.component.PrimaryButton
import com.example.taskmanager.core.ui.theme.AppTheme
import com.example.taskmanager.core.ui.component.label

@Composable
fun TeamListScreen(
    onNewTeam: () -> Unit,
    onTeam: (String) -> Unit,
    onProject: (projectId: String, teamId: String) -> Unit = { _, teamId -> onTeam(teamId) },
    vm: TeamListViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    CollectMessages(vm.messages)

    TeamListContent(
        state = state,
        onNewTeam = onNewTeam,
        onTeam = onTeam,
        onProject = onProject,
        onCreateProject = vm::createProject,
    )
}

@Composable
fun TeamListContent(
    state: TeamListUiState,
    onNewTeam: () -> Unit,
    onTeam: (String) -> Unit,
    onProject: (projectId: String, teamId: String) -> Unit = { _, _ -> },
    onCreateProject: (String, String) -> Unit = { _, _ -> },
) {
    var dialogTeamId by remember { mutableStateOf<String?>(null) }
    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Nhóm của tôi", style = MaterialTheme.typography.headlineMedium)
        when {
            state.isLoading -> CircularProgressIndicator()
            state.error != null -> Text(state.error)
            state.teams.isEmpty() -> Text("Chưa có nhóm nào")
            else -> LazyColumn(
                Modifier
                    .weight(1f)
                    .testTag(Tags.TEAM_LIST),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.teams, key = { it.id }) { t ->
                    val projects = state.projectsByTeam[t.id].orEmpty()
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onTeam(t.id) },
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(t.name, style = MaterialTheme.typography.titleMedium)
                                RoleBadge(t.role)
                            }
                            Text(
                                text = "${t.memberCount} thành viên",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                            )
                            projects.forEach { p ->
                                HorizontalDivider()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onProject(p.id, t.id) }
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(p.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "${p.taskCount} task",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            if (t.role != TeamRole.MEMBER) {
                                HorizontalDivider()
                                Text(
                                    text = "+ Thêm project",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { dialogTeamId = t.id }
                                        .padding(top = 10.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
        PrimaryButton(text = "+ Tạo nhóm mới", onClick = onNewTeam)
    }

    dialogTeamId?.let { teamId ->
        var projectName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { dialogTeamId = null },
            title = { Text("Thêm project mới") },
            text = {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    placeholder = { Text("Tên project...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (projectName.isNotBlank()) {
                            onCreateProject(teamId, projectName.trim())
                            dialogTeamId = null
                        }
                    }
                ) {
                    Text("Thêm")
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogTeamId = null }) {
                    Text("Hủy")
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamListPreview() {
    AppTheme {
        TeamListContent(
            TeamListUiState(false, listOf(TeamWithCounts("1", "Nhóm Android", TeamRole.OWNER, 4))),
            onNewTeam = {},
            onTeam = {},
        )
    }
}

@Composable
internal fun RoleBadge(role: TeamRole) {
    val (bg, fg) = when (role) {
        TeamRole.OWNER -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        TeamRole.ADMIN -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        TeamRole.MEMBER -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = bg,
        contentColor = fg,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = role.label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
