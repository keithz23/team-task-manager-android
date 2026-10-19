package com.example.taskmanager.feature.team.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.taskmanager.feature.team.NewTeamScreen
import com.example.taskmanager.feature.team.TeamDetailScreen
import com.example.taskmanager.feature.team.TeamListScreen
import com.example.taskmanager.navigation.Board
import com.example.taskmanager.navigation.NewTeam
import com.example.taskmanager.navigation.TeamDetail
import com.example.taskmanager.navigation.Teams

fun NavGraphBuilder.teamGraph(nav: NavHostController) {
    composable<Teams> {
        TeamListScreen(
            onNewTeam = { nav.navigate(NewTeam) },
            //onTeam = { nav.navigate(TeamDetail(it)) },
            onTeam = { teamId -> nav.navigate(Board(projectId = "project-demo-1", teamId = teamId)) },
        )
    }
    composable<NewTeam> { NewTeamScreen(onDone = { nav.popBackStack() }) }
    composable<TeamDetail> { entry ->
        val args = entry.toRoute<TeamDetail>()
        TeamDetailScreen(
            teamId = args.teamId,
            onProject = { projectId -> nav.navigate(Board(projectId, args.teamId)) },
            onBack = { nav.popBackStack() },
        )
    }
}
