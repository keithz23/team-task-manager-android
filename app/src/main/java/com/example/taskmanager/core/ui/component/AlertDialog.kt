package com.example.taskmanager.core.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    confirmText: String = "Xác nhận",
    dismissText: String = "Hủy",
    icon: ImageVector? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,

        icon = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null
                )
            }
        },

        title = {
            Text(text = dialogTitle)
        },

        text = {
            Text(text = dialogText)
        },

        confirmButton = {
            TextButton(
                onClick = onConfirmation
            ) {
                Text(text = confirmText)
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = dismissText)
            }
        }
    )
}