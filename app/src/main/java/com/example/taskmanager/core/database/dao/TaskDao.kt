package com.example.taskmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.database.relation.TaskWithAssignee
import com.example.taskmanager.core.model.TaskStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) cung cấp các phương thức thao tác với bảng `tasks` trong cơ sở dữ liệu Room.
 */
@Dao
interface TaskDao {

    /**
     * Thêm mới một công việc vào cơ sở dữ liệu.
     *
     * @param t Đối tượng [TaskEntity] cần thêm mới.
     */
    @Insert
    suspend fun insert(t: TaskEntity)

    /**
     * Cập nhật thông tin của một công việc.
     *
     * @param t Đối tượng [TaskEntity] chứa thông tin cần cập nhật.
     */
    @Update
    suspend fun update(t: TaskEntity)

    /**
     * Xóa một công việc dựa vào định danh của nó.
     *
     * @param id Identity của công việc cần xóa.
     */
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: String)

    /**
     * Cập nhật trạng thái của một công việc cụ thể.
     *
     * @param id Identity của công việc.
     * @param status Trạng thái mới [TaskStatus] cần cập nhật.
     */
    @Query("UPDATE tasks SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: TaskStatus)

    /**
     * Phân công hoặc hủy phân công người thực hiện cho một công việc.
     *
     * @param id Identity của công việc.
     * @param userId Identity của người dùng được phân công ([UserEntity.id]), hoặc `null` để hủy phân công.
     */
    @Query("UPDATE tasks SET assigneeId = :userId WHERE id = :id")
    suspend fun assign(id: String, userId: String?)

    /**
     * Lắng nghe danh sách các công việc thuộc một dự án theo trạng thái, kèm theo thông tin người thực hiện,
     * được sắp xếp theo thời gian tạo giảm dần.
     *
     * @param projectId Identity của dự án.
     * @param status Trạng thái [TaskStatus] của công việc cần lọc.
     * @return Một [Flow] phát ra danh sách [TaskWithAssignee] theo thời gian thực.
     */
    @Transaction
    @Query("SELECT * FROM tasks WHERE projectId = :projectId AND status = :status ORDER BY createdAt DESC")
    fun observeByStatus(projectId: String, status: TaskStatus): Flow<List<TaskWithAssignee>>

    /**
     * Lắng nghe số lượng công việc theo từng trạng thái trong một dự án cụ thể.
     *
     * @param projectId Identity của dự án cần thống kê.
     * @return Một [Flow] phát ra danh sách [StatusCount] chứa thống kê số lượng task cho mỗi trạng thái.
     */
    @Query("SELECT status, COUNT(*) AS count FROM tasks WHERE projectId = :projectId GROUP BY status")
    fun observeCounts(projectId: String): Flow<List<StatusCount>>

    /**
     * Lắng nghe chi tiết thông tin của một công việc kèm theo thông tin người thực hiện theo thời gian thực.
     *
     * @param id Identity của công việc cần quan sát.
     * @return Một [Flow] phát ra đối tượng [TaskWithAssignee?] tương ứng, hoặc `null` nếu không tìm thấy.
     */
    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observe(id: String): Flow<TaskWithAssignee?>

    /**
     * Lắng nghe danh sách các công việc được giao cho một người dùng cụ thể mà chưa hoàn thành (trạng thái khác 'DONE'),
     * được sắp xếp ưu tiên theo hạn chót (`dueDate`) gần nhất (các task không có hạn chót được đưa xuống cuối).
     *
     * @param userId Identity của người dùng.
     * @return Một [Flow] phát ra danh sách [TaskWithAssignee] theo thời gian thực.
     */
    @Transaction
    @Query("""
        SELECT * FROM tasks WHERE assigneeId = :userId AND status != 'DONE'
        ORDER BY dueDate IS NULL, dueDate
    """)
    fun observeMine(userId: String): Flow<List<TaskWithAssignee>>
}

/**
 * Data class đại diện cho kết quả thống kê số lượng công việc theo từng trạng thái (dùng cho truy vấn nhóm).
 *
 * @property status Trạng thái của công việc (kiểu [TaskStatus]).
 * @property count Tổng số lượng công việc ứng với trạng thái đó.
 */
data class StatusCount(val status: TaskStatus, val count: Int)
