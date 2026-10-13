package com.example.taskmanager.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Tạo bảng `users` (nếu không có `tableName` thì mặc định sẽ lấy tên class làm tên bảng)
 * `indicies = [Index(...)]` Khai báo index cho bảng để tối ưu truy vấn và tìm kiếm theo cột `email`
 *
 * @property id Unique identity của người dùng.
 * @property name Tên của người dùng.
 * @property email Địa chỉ email của người dùng.
 * @property passwordHash Mật khẩu đã được mã hóa (hashed password).
 * @property createdAt Thời điểm tài khoản được tạo.
 */

@Entity(tableName = "users", indices = [Index("email", unique = true)])
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)