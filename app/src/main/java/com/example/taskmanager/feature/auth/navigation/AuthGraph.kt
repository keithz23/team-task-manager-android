package com.example.taskmanager.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.taskmanager.feature.auth.LoginScreen
import com.example.taskmanager.feature.auth.RegisterScreen
import com.example.taskmanager.feature.mytasks.MyTasksScreen
import com.example.taskmanager.feature.profile.ProfileScreen
import com.example.taskmanager.navigation.Login
import com.example.taskmanager.navigation.MyTasks
import com.example.taskmanager.navigation.Profile
import com.example.taskmanager.navigation.Register
import com.example.taskmanager.navigation.TaskDetail

fun NavGraphBuilder.authGraph(nav: NavHostController) {
    composable<Login> {
        LoginScreen(
            onRegister = {
                nav.navigate(Register) {
                    launchSingleTop = true
                }
            }
        )
    }
    composable<Register> { RegisterScreen(onBackToLogin = { nav.popBackStack() }) }
    composable<Profile> { ProfileScreen() }
    composable<MyTasks> { MyTasksScreen(onTask = { nav.navigate(TaskDetail(it)) }) }
}

