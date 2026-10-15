package com.example.taskmanager.core.ui

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.Flow

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("SnackbarHostState chưa được cung cấp, kiểm tra AppNavHost")
}

/** Nghe một Flow<String> từ ViewModel và hiện mỗi phần tử thành Snackbar. */
@Composable
fun CollectMessages(messages: Flow<String>) {
    val snackbar = LocalSnackbarHostState.current
    LaunchedEffect(messages) {
        Log.d("SNACK", "collector started")
        messages.collect { text ->
            Log.d("SNACK", "UI received: $text")
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(text)
        }
    }
}
