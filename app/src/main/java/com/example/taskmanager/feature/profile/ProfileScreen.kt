package com.example.taskmanager.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.taskmanager.feature.auth.AuthViewModel
import androidx.compose.runtime.setValue
import com.example.taskmanager.core.ui.component.Avatar
import com.example.taskmanager.core.ui.component.ConfirmDialog

@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    vm: AuthViewModel = hiltViewModel()
) {
    val currentUserId by vm.currentUserId.collectAsState()
    val user = vm.user

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            vm.getUserById(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (vm.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Spacer(modifier = Modifier.height(32.dp))

            // Avatar
            Avatar(user)

            Spacer(modifier = Modifier.height(16.dp))

            // Tên người dùng
            Text(
                text = user?.name ?: "Nguyễn Văn An",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Email người dùng
            Text(
                text = user?.email ?: "an@example.com",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card chứa thông tin
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    // Số nhóm
                    ProfileInfoRow(
                        label = "Số nhóm",
                        value = "2"
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Task đã hoàn thành
                    ProfileInfoRow(
                        label = "Task đã hoàn thành",
                        value = "7"
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Giao diện tối
                    ProfileInfoRow(
                        label = "Giao diện tối",
                        value = "Tắt"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút Đăng xuất
            OutlinedButton(
                onClick = {
                    showLogoutDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "Đăng xuất",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            onConfirmation = {
                showLogoutDialog = false

                vm.logout()
                onLogout()
            },
            dialogTitle = "Đăng xuất",
            dialogText = "Bạn có chắc chắn muốn đăng xuất không?",
            confirmText = "Đăng xuất",
            dismissText = "Hủy"
        )
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}