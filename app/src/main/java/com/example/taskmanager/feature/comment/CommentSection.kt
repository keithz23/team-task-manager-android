package com.example.taskmanager.feature.comment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskmanager.core.ui.Tags
import com.example.taskmanager.core.ui.component.AppTextField
import com.example.taskmanager.core.ui.component.AvatarInitials
import com.example.taskmanager.core.util.toRelativeTime

/**
 * Khối bình luận để nhúng vào TaskDetail: CommentSection(taskId).
 * Không tự cuộn (dùng Column, không phải LazyColumn) nên đặt được trong màn hình đã có
 * verticalScroll hoặc trong một item { } của LazyColumn.
 */
@Composable
fun CommentSection(taskId: String, modifier: Modifier = Modifier) {
    val vm = hiltViewModel<CommentViewModel, CommentViewModel.Factory>(
        key = "comments-$taskId",
        creationCallback = { it.create(taskId) },
    )
    val state by vm.state.collectAsStateWithLifecycle()
    CommentSectionContent(state, vm::onDraftChange, vm::send, modifier)
}

@Composable
fun CommentSectionContent(
    state: CommentUiState,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Bình luận (${state.comments.size})", style = MaterialTheme.typography.titleMedium)

        Column(Modifier.testTag(Tags.COMMENT_LIST), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (state.comments.isEmpty()) {
                Text(
                    "Chưa có bình luận nào",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.comments.forEach { CommentItem(it) }
        }

        Row(verticalAlignment = Alignment.Top) {
            AppTextField(
                value = state.draft,
                onValueChange = onDraftChange,
                placeholder = "Viết bình luận…",
                modifier = Modifier.weight(1f),
                error = state.error,
                singleLine = false,
                testTag = Tags.COMMENT_INPUT,
            )
            IconButton(
                onClick = onSend,
                enabled = state.canSend,
                modifier = Modifier.testTag(Tags.COMMENT_SEND),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi bình luận")
            }
        }
    }
}

@Composable
private fun CommentItem(comment: CommentUi) {
    Row(Modifier.fillMaxWidth().testTag(Tags.commentItem(comment.id))) {
        AvatarInitials(comment.authorName)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (comment.isMine) "${comment.authorName} (bạn)" else comment.authorName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    comment.createdAt.toRelativeTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(comment.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
