package com.example.taskmanager

import com.example.taskmanager.core.database.entity.CommentEntity
import com.example.taskmanager.core.database.relation.CommentWithAuthor
import com.example.taskmanager.feature.comment.CommentViewModel
import com.example.taskmanager.util.FakeCommentRepository
import com.example.taskmanager.util.FakeSessionManager
import com.example.taskmanager.util.MainDispatcherRule
import com.example.taskmanager.util.testUser
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CommentViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val an = testUser("u-an", "Nguyễn An")
    private val binh = testUser("u-binh", "Trần Bình")

    private val seed = listOf(
        CommentWithAuthor(CommentEntity("c1", "t1", an.id, "Comment của An", 1L), an),
        CommentWithAuthor(CommentEntity("c2", "t1", binh.id, "Comment của Bình", 2L), binh),
        CommentWithAuthor(CommentEntity("c3", "t2", binh.id, "Task khác", 3L), binh),
    )

    private val repo = FakeCommentRepository(author = an, initial = seed)
    private val session = FakeSessionManager(initialUserId = an.id)

    /** Tạo ViewModel và giữ một collector để state (WhileSubscribed) luôn được cập nhật. */
    private fun TestScope.createVm(): CommentViewModel {
        val vm = CommentViewModel("t1", repo, session)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        return vm
    }

    @Test
    fun loadsOnlyCommentsOfTask_andMarksMine() = runTest {
        val vm = createVm()

        val comments = vm.state.value.comments
        assertEquals(listOf("c1", "c2"), comments.map { it.id })
        assertEquals("Nguyễn An", comments[0].authorName)
        assertTrue(comments[0].isMine)
        assertFalse(comments[1].isMine)
    }

    @Test
    fun loggedOut_nothingIsMine() = runTest {
        session.clear()
        val vm = createVm()

        assertTrue(vm.state.value.comments.none { it.isMine })
    }

    @Test
    fun blankDraft_cannotSend() = runTest {
        val vm = createVm()

        assertFalse(vm.state.value.canSend)
        vm.onDraftChange("   ")
        assertFalse(vm.state.value.canSend)
        vm.send()
        assertTrue(repo.added.isEmpty())
    }

    @Test
    fun sendSuccess_clearsDraft_andCommentAppears() = runTest {
        val vm = createVm()

        vm.onDraftChange("Xong phần UI")
        assertTrue(vm.state.value.canSend)
        vm.send()
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals("", s.draft)
        assertFalse(s.isSending)
        assertNull(s.error)
        assertEquals(listOf("t1" to "Xong phần UI"), repo.added)
        assertEquals("Xong phần UI", s.comments.last().content)
        assertTrue(s.comments.last().isMine)
    }

    @Test
    fun whileSending_isSendingAndCannotSendAgain() = runTest {
        val gate = CompletableDeferred<Unit>()
        repo.gate = gate
        val vm = createVm()

        vm.onDraftChange("Đang gửi")
        vm.send()
        assertTrue(vm.state.value.isSending)
        assertFalse(vm.state.value.canSend)

        vm.send() // bấm lần 2 khi đang gửi: bị bỏ qua
        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(1, repo.added.size)
        assertFalse(vm.state.value.isSending)
    }

    @Test
    fun sendFailure_keepsDraft_andShowsError() = runTest {
        repo.failNext = IllegalStateException("Chưa đăng nhập")
        val vm = createVm()

        vm.onDraftChange("Nội dung")
        vm.send()
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals("Nội dung", s.draft)
        assertEquals("Chưa đăng nhập", s.error)
        assertFalse(s.isSending)
        assertEquals(2, s.comments.size)
    }

    @Test
    fun typingAfterError_clearsError() = runTest {
        repo.failNext = IllegalStateException("Lỗi")
        val vm = createVm()
        vm.onDraftChange("abc")
        vm.send()
        advanceUntilIdle()

        vm.onDraftChange("abcd")

        assertNull(vm.state.value.error)
    }
}
