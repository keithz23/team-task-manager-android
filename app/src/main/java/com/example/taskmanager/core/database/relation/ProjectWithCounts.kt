package com.example.taskmanager.core.database.relation

/**
 * Data class đại diện cho thông tin tóm tắt của một dự án ([Project]), bao gồm tổng số lượng
 * công việc (task) thuộc dự án đó (thường dùng cho kết quả truy vấn kết hợp và thống kê).
 *
 * @property id Unique identity của dự án.
 * @property name Tên của dự án.
 * @property taskCount Tổng số lượng công việc (task) có trong dự án.
 */

data class ProjectWithCount(
    val id: String,
    val name: String,
    val taskCount: Int,
)
