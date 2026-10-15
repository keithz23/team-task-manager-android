package com.example.taskmanager.feature.mytasks

import androidx.compose.runtime.Composable
import com.example.taskmanager.core.ui.StubScreen

@Composable fun MyTasksScreen(onTask: (String) -> Unit) {
    StubScreen("MyTasks")
}

