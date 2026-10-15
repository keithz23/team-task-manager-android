package com.example.taskmanager.feature.team

import androidx.compose.runtime.Composable
import com.example.taskmanager.core.ui.StubScreen

@Composable fun TeamDetailScreen(teamId: String, onProject: (String) -> Unit, onBack: () -> Unit) {
    StubScreen("TeamDetail")
}
