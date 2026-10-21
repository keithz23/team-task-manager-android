package com.example.taskmanager.feature.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.core.data.CommentRepository
import com.example.taskmanager.core.database.relation.CommentWithAuthor
import com.example.taskmanager.core.session.AuthState
import com.example.taskmanager.core.session.SessionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Một bình luận đã sẵn sàng để hiển thị. [isMine] = do user đang đăng nhập viết. */
data class CommentUi(
    val id: String,
    val authorName: String,
    val content: String,
    val createdAt: Long,
    val isMine: Boolean,
)

data class CommentUiState(
    val comments: List<CommentUi> = emptyList(),
    val draft: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
) {
    val canSend: Boolean get() = draft.isNotBlank() && !isSending
}

/**
 * Danh sách + ô nhập bình luận của một task. taskId truyền vào qua [Factory]
 * (xem CommentSection), unit test thì gọi constructor trực tiếp.
 */
@HiltViewModel(assistedFactory = CommentViewModel.Factory::class)
class CommentViewModel @AssistedInject constructor(
    @Assisted private val taskId: String,
    private val repo: CommentRepository,
    session: SessionManager,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(taskId: String): CommentViewModel
    }

    // Phần do người dùng nhập (draft, đang gửi, lỗi); danh sách comment lấy từ DB.
    private val form = MutableStateFlow(CommentUiState())

    val state: StateFlow<CommentUiState> = combine(
        repo.observe(taskId),
        session.authState,
        form,
    ) { comments, auth, f ->
        val me = (auth as? AuthState.LoggedIn)?.userId
        f.copy(comments = comments.map { it.toUi(me) })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CommentUiState())

    fun onDraftChange(text: String) = form.update { it.copy(draft = text, error = null) }

    /** Gửi draft. Thành công thì xoá draft; lỗi thì giữ nguyên draft để người dùng sửa và gửi lại. */
    fun send() {
        val draft = form.value.draft
        if (!form.value.canSend) return
        form.update { it.copy(isSending = true, error = null) }
        viewModelScope.launch {
            repo.add(taskId, draft)
                .onSuccess { form.update { it.copy(draft = "", isSending = false) } }
                .onFailure { e ->
                    form.update { it.copy(isSending = false, error = e.message ?: "Không gửi được bình luận") }
                }
        }
    }
}

private fun CommentWithAuthor.toUi(currentUserId: String?) = CommentUi(
    id = comment.id,
    authorName = author.name,
    content = comment.content,
    createdAt = comment.createdAt,
    isMine = comment.authorId == currentUserId,
)
