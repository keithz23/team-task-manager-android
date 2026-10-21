package com.example.taskmanager.feature.board

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.taskmanager.core.database.relation.MemberWithUser
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.ui.component.AppTextField
import com.example.taskmanager.core.ui.component.AvatarInitials
import com.example.taskmanager.core.ui.component.PrimaryButton
import com.example.taskmanager.core.ui.component.PriorityChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormSheet(
    members: List<MemberWithUser> = emptyList(),
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String?, priority: Priority, dueDate: Long?, assigneeId: String?) -> Unit,
) {
    var titleInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var priorityInput by remember { mutableStateOf(Priority.MEDIUM) }
    var dueDateInput by remember { mutableStateOf<Long?>(null) }
    var assigneeInput by remember { mutableStateOf<String?>(null) }
    var textInput by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf<String?>(null) }
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ){
            Text(
                text = "Create A New Task",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            AppTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = "Task Title"
            )
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Task Description") },
                modifier = Modifier.fillMaxWidth()
            )
            PriorityChip(
                modifier = Modifier.padding(top = 16.dp),
                onClick = { /*TODO*/ },
                selected = false,
                priority = com.example.taskmanager.core.model.Priority.HIGH
            )
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(
                text = "Create Task",
                onClick = {
                    if (titleInput.isNotBlank()) {
                        titleError = ""
                    } else {
                        onSubmit(
                            titleInput.trim(),
                            descriptionInput.trim().ifEmpty { null },
                            priorityInput,
                            dueDateInput,
                            assigneeInput
                        )
                        onDismiss()
                    }
                }
            )
        }
    }

}
