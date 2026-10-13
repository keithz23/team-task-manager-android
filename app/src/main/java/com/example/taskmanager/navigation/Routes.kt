package com.example.taskmanager.navigation

import kotlinx.serialization.Serializable

@Serializable object Login
@Serializable object Register

@Serializable object Teams        // tab 1
@Serializable object MyTasks      // tab 2
@Serializable object Profile      // tab 3

@Serializable object NewTeam
@Serializable data class TeamDetail(val teamId: String)
@Serializable data class Board(val projectId: String, val teamId: String)
@Serializable data class TaskDetail(val taskId: String)
@Serializable data class AssigneePicker(val teamId: String)