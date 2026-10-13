package com.example.taskmanager.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import java.util.UUID

/**
 * Đại diện cho bảng `tasks` trong DB Room, lưu trữ thông tin về các công việc (task) trong dự án.
 *
 * Khóa chính của bảng là [id], được tự động tạo giá trị ngẫu nhiên dưới dạng UUID.
 *
 * Các thiết lập quan trọng bao gồm:
 * - Khóa ngoại liên kết tới bảng `ProjectEntity` (thông qua [projectId]), với việc tự động xóa ([ForeignKey.CASCADE]) khi dự án cha bị xóa.
 * - Khóa ngoại liên kết tới bảng `UserEntity` (thông qua [assigneeId]), với việc đặt giá trị thành `null` ([ForeignKey.SET_NULL]) khi người được phân công bị xóa.
 * - Các chỉ mục ([Index]) được tạo trên cặp cột ([projectId], [status]) và cột [assigneeId] để tối ưu hóa hiệu suất truy vấn.
 *
 * @property id Unique identity của task, đóng vai trò là khóa chính.
 * @property projectId Identity của dự án chứa task này, tham chiếu đến [ProjectEntity.id].
 * @property title Tiêu đề của task.
 * @property description Mô tả chi tiết về nội dung task (mặc định là chuỗi rỗng).
 * @property status Trạng thái hiện tại của task (mặc định là [TaskStatus.TODO]).
 * @property priority Mức độ ưu tiên của task (mặc định là [Priority.MEDIUM]).
 * @property dueDate Thời hạn hoàn thành task (dạng timestamp, có thể là `null`).
 * @property assigneeId Định danh của người dùng được phân công thực hiện task, tham chiếu đến [UserEntity.id] (có thể là `null`).
 * @property createdAt Thời điểm task được khởi tạo.
 */

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(ProjectEntity::class, ["id"], ["projectId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(UserEntity::class, ["id"], ["assigneeId"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("projectId", "status"), Index("assigneeId")],
)
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: Long? = null,
    val assigneeId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)