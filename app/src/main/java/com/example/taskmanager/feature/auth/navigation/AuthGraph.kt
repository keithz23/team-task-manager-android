package com.example.taskmanager.feature.auth.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
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
                if (nav.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    val isLogin = nav.currentBackStackEntry?.destination?.hasRoute<Login>() == true
                    if (isLogin) {
                        nav.navigate(Register) {
                            launchSingleTop = true
                        }
                    }
                }
            }
        )
    }
    composable<Register> {
        RegisterScreen(
            onBackToLogin = {
                if (nav.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    val isRegister = nav.currentBackStackEntry?.destination?.hasRoute<Register>() == true
                    if (isRegister) {
                        if (!nav.popBackStack(route = Login, inclusive = false)) {
                            nav.navigate(Login) {
                                popUpTo<Register> { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            }
        )
    }
    composable<Profile> { ProfileScreen() }
    composable<MyTasks> { MyTasksScreen(onTask = { nav.navigate(TaskDetail(it)) }) }
}


