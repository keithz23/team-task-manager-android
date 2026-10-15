package com.example.taskmanager.feature.team

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskmanager.core.database.relation.TeamWithCounts
import com.example.taskmanager.core.model.TeamRole
import com.example.taskmanager.core.ui.Tags
import com.example.taskmanager.core.ui.theme.AppTheme

@Composable
fun TeamListScreen(
    onNewTeam: () -> Unit,
    onTeam: (String) -> Unit,
    vm: TeamListViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    TeamListContent(state, onNewTeam, onTeam)
}

@Composable
fun TeamListContent(state: TeamListUiState, onNewTeam: () -> Unit, onTeam: (String) -> Unit) {
    Column (
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Nhóm của tôi", style = MaterialTheme.typography.headlineMedium)
        when {
            state.isLoading -> CircularProgressIndicator()
            state.error != null -> Text(state.error)
            state.teams.isEmpty() -> Text("Chưa có nhóm nào")
            else -> LazyColumn (
                Modifier.weight(1f).testTag(Tags.TEAM_LIST),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.teams, key = { it.id }) { t ->
                    Card (Modifier.fillMaxWidth().clickable { onTeam(t.id) }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(t.name, style = MaterialTheme.typography.titleMedium)
                            Text("${t.memberCount} thành viên")
                        }
                    }
                }
            }
        }
        Button(onClick = onNewTeam, modifier = Modifier.fillMaxWidth()) { Text("+ Tạo nhóm mới") }
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamListPreview() {
    AppTheme() {
   TeamListContent(
       TeamListUiState(false, listOf(TeamWithCounts("1", "Nhóm Android", TeamRole.OWNER, 4))),
       onNewTeam = {}, onTeam = {},
   )
    }
}