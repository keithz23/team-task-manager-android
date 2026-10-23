package com.example.taskmanager.feature.team

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskmanager.core.ui.CollectMessages
import com.example.taskmanager.core.ui.Tags
import com.example.taskmanager.core.ui.component.AppTextField
import com.example.taskmanager.core.ui.component.AvatarInitials
import com.example.taskmanager.core.ui.component.PrimaryButton
import com.example.taskmanager.core.ui.theme.AppTheme
import com.example.taskmanager.core.ui.component.label
import com.example.taskmanager.core.model.TeamRole

@Composable
fun NewTeamScreen(
    onDone: () -> Unit,
    vm: NewTeamViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    CollectMessages(vm.messages)

    NewTeamContent(
        state = state,
        onNameChange = vm::onNameChange,
        onFirstProjectChange = vm::onFirstProjectChange,
        onEmailInputChange = vm::onEmailInputChange,
        onAddEmail = vm::addEmail,
        onSubmit = { vm.createTeam(onDone) },
        onBack = onDone,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTeamContent(
    state: NewTeamUiState,
    onNameChange: (String) -> Unit,
    onFirstProjectChange: (String) -> Unit,
    onEmailInputChange: (String) -> Unit = {},
    onAddEmail: () -> Unit = {},
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nhóm",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag(Tags.BACK_BUTTON)) {
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
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Tạo nhóm mới", style = MaterialTheme.typography.headlineSmall)

            AppTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = "Tên nhóm",
                placeholder = "Nhóm Đồ án Android",
                error = state.nameError,
                testTag = Tags.TEAM_NAME,
            )

            AppTextField(
                value = state.firstProject,
                onValueChange = onFirstProjectChange,
                label = "Project đầu tiên",
                placeholder = "Sprint đồ án",
            )

            Text(
                text = "Thành viên (${1 + state.pendingEmails.size})",
                style = MaterialTheme.typography.titleSmall,
            )

            MemberItemCard(name = "Bạn", role = TeamRole.OWNER.label)

            state.pendingEmails.forEach { email ->
                MemberItemCard(name = email, role = TeamRole.MEMBER.label)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = state.emailInput,
                    onValueChange = onEmailInputChange,
                    placeholder = { Text("Email thành viên...") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(
                    onClick = onAddEmail,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.height(52.dp),
                ) {
                    Text("Thêm", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Tạo nhóm",
                onClick = onSubmit,
                loading = state.isLoading,
                modifier = Modifier.testTag(Tags.CREATE_TEAM_BUTTON)
            )
        }
    }
}

@Composable
internal fun MemberItemCard(name: String, role: String) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AvatarInitials(name = name, size = 36.dp)
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NewTeamPreview() {
    AppTheme {
        NewTeamContent(
            state = NewTeamUiState(name = "Nhóm đò án Android", firstProject = "Sprint đồ án"),
            onNameChange = {},
            onFirstProjectChange = {},
            onSubmit = {},
            onBack = {},
        )
    }
}
