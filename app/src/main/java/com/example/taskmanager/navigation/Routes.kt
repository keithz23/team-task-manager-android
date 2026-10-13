package com.example.taskmanager.navigation

@kotlinx.serialization.Serializable object Login
@kotlinx.serialization.Serializable object Register

@kotlinx.serialization.Serializable object Teams        // tab 1
@kotlinx.serialization.Serializable object MyTasks      // tab 2
@kotlinx.serialization.Serializable object Profile      // tab 3

@kotlinx.serialization.Serializable object NewTeam
@kotlinx.serialization.Serializable data class TeamDetail(val teamId: String)
@kotlinx.serialization.Serializable data class Board(val projectId: String, val teamId: String)
@kotlinx.serialization.Serializable data class TaskDetail(val taskId: String)
@Serializable data class AssigneePicker(val teamId: String)

