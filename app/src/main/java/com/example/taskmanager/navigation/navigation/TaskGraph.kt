package com.example.taskmanager.navigation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.taskmanager.feature.board.BoardScreen
import com.example.taskmanager.feature.taskdetail.TaskDetailScreen
import com.example.taskmanager.navigation.Board
import com.example.taskmanager.navigation.TaskDetail

fun NavGraphBuilder.taskGraph(nav: NavHostController) {
    composable<Board> { entry ->
        val args = entry.toRoute<Board>()
        BoardScreen(
            projectId = args.projectId,
            teamId = args.teamId,
            onTask = { nav.navigate(TaskDetail(it)) },
            onBack = { nav.popBackStack() },
        )
    }
    composable<TaskDetail> { TaskDetailScreen(onBack = { nav.popBackStack() }) }
}
