package com.example.taskmanager

import com.example.taskmanager.core.database.entity.ProjectEntity
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.feature.mytasks.MyTasksViewModel
import com.example.taskmanager.util.FakeTaskRepository
import com.example.taskmanager.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyTasksViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val taskRepo = FakeTaskRepository()

    @Test
    fun tasks_emitsMineTasksFromRepository() = runTest {
        val vm = MyTasksViewModel(taskRepo)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.tasks.collect {}
        }

        assertEquals(emptyList<TaskWithAssignee>(), vm.tasks.value)

        val task1 = TaskWithAssignee(
            task = TaskEntity(id = "t1", projectId = "p1", title = "Thiết kế schema Room", priority = Priority.HIGH),
            assignee = null,
            project = ProjectEntity(id = "p1", teamId = "team1", name = "Sprint đồ án"),
        )
        val task2 = TaskWithAssignee(
            task = TaskEntity(id = "t2", projectId = "p1", title = "Viết Repository test", priority = Priority.MEDIUM),
            assignee = null,
            project = ProjectEntity(id = "p1", teamId = "team1", name = "Sprint đồ án"),
        )

        taskRepo.mineTasksFlow.value = listOf(task1, task2)
        advanceUntilIdle()

        assertEquals(2, vm.tasks.value.size)
        assertEquals("Thiết kế schema Room", vm.tasks.value[0].task.title)
        assertEquals("Viết Repository test", vm.tasks.value[1].task.title)
    }
}
