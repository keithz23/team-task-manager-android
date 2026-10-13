package com.example.taskmanager.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Tạo bảng teams (nếu không có `tableName` thì mặc dđịnh sẽ lấy tên class làm tên bảng)
 */
@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
