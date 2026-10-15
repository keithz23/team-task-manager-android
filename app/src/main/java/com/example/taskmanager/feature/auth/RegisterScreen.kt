package com.example.taskmanager.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.taskmanager.core.ui.component.AppTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Tạo tài khoản",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            // Subtitle
            Text(
                text = "Bắt đầu quản lý công việc nhóm",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Full name
            AppTextField(
                value = vm.registerFullName,
                onValueChange = { vm.registerFullName = it; vm.clearError() },
                label = "Họ tên",
                placeholder = "Nguyen Van A",
                error = vm.fullNameError,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            AppTextField(
                value = vm.registerEmail,
                onValueChange = { vm.registerEmail = it; vm.clearError() },
                label = "Email",
                placeholder = "nguyenvana@gmail.com",
                error = vm.emailError,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            AppTextField(
                value = vm.registerPassword,
                onValueChange = { vm.registerPassword = it; vm.clearError() },
                label = "Mật khẩu",
                placeholder = "******",
                error = vm.passwordError,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            AppTextField(
                value = vm.registerConfirmPassword,
                onValueChange = { vm.registerConfirmPassword = it; vm.clearError() },
                label = "Xác nhận mật khẩu",
                placeholder = "******",
                error = vm.confirmPasswordError,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nút đăng ký
            Button(
                onClick = vm::register,
                enabled = !vm.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (vm.isLoading) "Đang xử lý..." else "Đăng ký",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Đã có tài khoản? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { onBackToLogin() },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Đăng nhập",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}