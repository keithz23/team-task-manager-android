package com.example.taskmanager.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.taskmanager.core.session.AuthState
import com.example.taskmanager.core.session.SessionManager
import com.example.taskmanager.core.ui.LocalSnackbarHostState
import com.example.taskmanager.feature.auth.navigation.authGraph
import com.example.taskmanager.feature.team.navigation.teamGraph
import com.example.taskmanager.navigation.navigation.taskGraph

private data class Tab(
    val route: Any,
    val label: String,
    val icon: ImageVector,
    val isSelected: (NavDestination?) -> Boolean,
)

private val tabs = listOf(
    Tab(Teams, "Nhóm", Icons.Default.Home) { it?.hasRoute<Teams>() == true },
    Tab(MyTasks, "Việc của tôi", Icons.Default.Check) { it?.hasRoute<MyTasks>() == true },
    Tab(Profile, "Cá nhân", Icons.Default.Person) { it?.hasRoute<Profile>() == true },
)

@Composable
fun AppNavHost(session: SessionManager) {
    val auth by session.authState.collectAsStateWithLifecycle(initialValue = AuthState.Loading)
    val snackbarHostState = remember { SnackbarHostState() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (auth) {
            AuthState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            else -> {
                val loggedIn = auth is AuthState.LoggedIn
                key(loggedIn) { MainScaffold(loggedIn, snackbarHostState) }
            }
        }
    }
}

@Composable
private fun MainScaffold(loggedIn: Boolean, snackbarHostState: SnackbarHostState) {
    val nav = rememberNavController()
    val dest = nav.currentBackStackEntryAsState().value?.destination

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        actionColor = MaterialTheme.colorScheme.onError,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.imePadding(),
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (tabs.any { it.isSelected(dest) }) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        tabs.forEach { tab ->
                            val selected = tab.isSelected(dest)

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    nav.navigate(tab.route) {
                                        popUpTo(nav.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent,

                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,

                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

            },
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = if (loggedIn) Teams else Login,
                modifier = Modifier.padding(padding),
            ) {
                authGraph(nav)
                teamGraph(nav)
                taskGraph(nav)
            }
        }
    }

}