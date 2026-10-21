package com.example.taskmanager.feature.board

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.taskmanager.core.database.relation.MemberWithUser
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.ui.Tags
import com.example.taskmanager.core.ui.component.AppTextField
import com.example.taskmanager.core.ui.component.AvatarInitials
import com.example.taskmanager.core.ui.component.PrimaryButton
import com.example.taskmanager.core.ui.component.PriorityChip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormSheet(
    members: List<MemberWithUser> = emptyList(),
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String?, priority: Priority, dueDate: Long?, assigneeId: String?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedDueDate by remember { mutableStateOf<Long?>(null) }
    var selectedAssigneeId by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Tạo Task mới",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            AppTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = null
                },
                label = "Task Title",
                placeholder = "Nhập title của task...",
                error = titleError,
                testTag = Tags.TASK_TITLE
            )
            AppTextField(
                value = description,
                onValueChange = { description = it },
                label = "Mô tả",
                placeholder = "Nhập mô tả tại đây...",
                singleLine = false,
                minLines = 3
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Mức độ ưu tiên",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { p ->
                        PriorityChip(
                            priority = p,
                            selected = (selectedPriority == p),
                            onClick = { selectedPriority = p }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Due date",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = selectedDueDate?.let { formatDueDate(it) } ?: "Select due date"
                        )
                    }

                    if (selectedDueDate != null) {
                        IconButton(onClick = { selectedDueDate = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete due date",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (members.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Assignee",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = (selectedAssigneeId == null),
                            onClick = { selectedAssigneeId = null },
                            label = { Text("Unassigned") }
                        )

                        members.forEach { m ->
                            FilterChip(
                                selected = (selectedAssigneeId == m.user.id),
                                onClick = { selectedAssigneeId = m.user.id },
                                leadingIcon = {
                                    AvatarInitials(name = m.user.name, size = 24.dp)
                                },
                                label = { Text(m.user.name) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryButton(
                text = "Tạo mới",
                onClick = {
                    if (title.isBlank()) {
                        titleError = "Vui lòng nhập title để tiếp tục"
                    } else {
                        onSubmit(
                            title.trim(),
                            description.trim().ifEmpty { null },
                            selectedPriority,
                            selectedDueDate,
                            selectedAssigneeId
                        )
                        onDismiss()
                    }
                }
            )
        }
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDueDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDueDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Void")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun formatDueDate(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
