package com.example.taskmanager.feature.taskdetail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.database.relation.MemberWithUser
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.ui.component.AppTextField
import com.example.taskmanager.core.ui.component.AvatarInitials
import com.example.taskmanager.core.ui.component.ConfirmDialog
import com.example.taskmanager.core.ui.component.PrimaryButton
import com.example.taskmanager.core.ui.component.PriorityChip
import com.example.taskmanager.core.ui.component.label
import com.example.taskmanager.feature.comment.CommentSection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.FilterChipDefaults
import com.example.taskmanager.core.ui.theme.tagColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    onBack: () -> Unit,
    vm: TaskDetailViewModel = hiltViewModel(),
) {
    val taskWithAssignee by vm.task.collectAsStateWithLifecycle()
    val canEdit by vm.canEdit.collectAsStateWithLifecycle()
    val canDelete by vm.canDelete.collectAsStateWithLifecycle()
    val members by vm.members.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showConfirmDoneDialog by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chi tiết công việc",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại trang trước",
                        )
                    }
                },
                actions = {
                    if (canEdit) {
                        IconButton(onClick = { showEditSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Chỉnh sửa công việc",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    if (canDelete) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xóa công việc",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        }
    ) { padding ->
        taskWithAssignee?.let { item ->
            val task = item.task
            val assignee = item.assignee

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                if (!canEdit) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = "🔒 Bạn đang ở chế độ chỉ đọc. Chỉ người thực hiện hoặc Trưởng nhóm mới có quyền chỉnh sửa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Trạng thái",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TaskStatus.entries.forEach { status ->
                            val isSelected = (task.status == status)
                            FilterChip(
                                selected = isSelected,
                                enabled = canEdit,
                                onClick = {
                                    if (status == TaskStatus.DONE && task.status != TaskStatus.DONE) {
                                        showConfirmDoneDialog = true
                                    } else {
                                        vm.updateStatus(status)
                                    }
                                },
                                label = { Text(status.label) },
                                // Cùng màu với nhãn trạng thái trên TaskCard
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = status.tagColors().container,
                                    selectedLabelColor = status.tagColors().content,
                                ),
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Độ ưu tiên:", style = MaterialTheme.typography.bodyMedium)
                            PriorityChip(priority = task.priority)
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Người thực hiện:", style = MaterialTheme.typography.bodyMedium)
                            if (assignee != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AvatarInitials(name = assignee.name, size = 26.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(assignee.name, fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Text(
                                    "Chưa phân công",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Hạn chót:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = task.dueDate?.let { formatDateTime(it) } ?: "Chưa thiết lập",
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Mô tả",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = task.description.ifEmpty { "Chưa có mô tả cho công việc này." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (task.description.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    )
                }

                HorizontalDivider()

                CommentSection(taskId = vm.taskId)

                Spacer(Modifier.height(16.dp))
            }

            if (showEditSheet) {
                EditTaskBottomSheet(
                    task = task,
                    members = members,
                    onDismiss = { showEditSheet = false },
                    onSave = { title, desc, priority, dueDate, assigneeId ->
                        vm.updateTask(title, desc, priority, dueDate, assigneeId)
                        showEditSheet = false
                    },
                )
            }
        } ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirmation = {
                showDeleteDialog = false
                vm.deleteTask(onSuccess = onBack)
            },
            dialogTitle = "Xóa công việc",
            dialogText = "Bạn có chắc chắn muốn xóa công việc này không? Thao tác này không thể hoàn tác.",
            confirmText = "Xóa",
            dismissText = "Hủy",
        )
    }

    if (showConfirmDoneDialog) {
        ConfirmDialog(
            onDismissRequest = { showConfirmDoneDialog = false },
            onConfirmation = {
                showConfirmDoneDialog = false
                vm.updateStatus(TaskStatus.DONE)
            },
            dialogTitle = "Xác nhận hoàn thành",
            dialogText = "Bạn có chắc chắn muốn đánh dấu công việc này là đã hoàn thành không?",
            confirmText = "Hoàn thành",
            dismissText = "Hủy",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTaskBottomSheet(
    task: TaskEntity,
    members: List<MemberWithUser>,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String?, priority: Priority, dueDate: Long?, assigneeId: String?) -> Unit,
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var selectedPriority by remember { mutableStateOf(task.priority) }
    var selectedDueDate by remember { mutableStateOf(task.dueDate) }
    var selectedAssigneeId by remember { mutableStateOf(task.assigneeId) }
    var showDatePicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        contentWindowInsets = { BottomSheetDefaults.windowInsets.union(WindowInsets.ime) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Chỉnh sửa công việc",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            AppTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = null
                },
                label = "Tên công việc",
                placeholder = "Nhập tên công việc...",
                error = titleError,
            )
            AppTextField(
                value = description,
                onValueChange = { description = it },
                label = "Mô tả chi tiết",
                placeholder = "Nhập mô tả cho công việc...",
                singleLine = false,
                minLines = 3,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Độ ưu tiên",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { p ->
                        PriorityChip(
                            priority = p,
                            selected = (selectedPriority == p),
                            onClick = { selectedPriority = p },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Hạn chót",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = selectedDueDate?.let { formatDateTime(it) } ?: "Chọn hạn chót",
                        )
                    }
                    if (selectedDueDate != null) {
                        IconButton(onClick = { selectedDueDate = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Xóa hạn chót",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }

            if (members.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Người thực hiện",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
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
                            label = { Text("Chưa phân công") },
                        )
                        members.forEach { m ->
                            FilterChip(
                                selected = (selectedAssigneeId == m.user.id),
                                onClick = { selectedAssigneeId = m.user.id },
                                leadingIcon = {
                                    AvatarInitials(name = m.user.name, size = 24.dp)
                                },
                                label = { Text(m.user.name) },
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(
                text = "Lưu thay đổi",
                onClick = {
                    if (title.isBlank()) {
                        titleError = "Tên công việc không được để trống"
                    } else {
                        onSave(
                            title.trim(),
                            description.trim().ifEmpty { null },
                            selectedPriority,
                            selectedDueDate,
                            selectedAssigneeId,
                        )
                    }
                },
            )
        }
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDueDate ?: System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDueDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    },
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun formatDateTime(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))