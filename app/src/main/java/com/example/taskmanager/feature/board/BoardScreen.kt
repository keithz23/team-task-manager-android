package com.example.taskmanager.feature.board

import androidx.compose.runtime.Composable
import com.example.taskmanager.core.ui.StubScreen

@Composable fun BoardScreen(projectId: String, teamId: String, onTask: (String) -> Unit, onBack: () -> Unit) {
    StubScreen("Board")
}
