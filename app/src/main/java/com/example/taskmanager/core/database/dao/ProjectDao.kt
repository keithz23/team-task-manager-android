package com.example.taskmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.taskmanager.core.database.entity.ProjectEntity
import com.example.taskmanager.core.database.relation.ProjectWithCount
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) cung cấp các phương thức thao tác với bảng `projects` trong cơ sở dữ liệu Room.
 */
@Dao
interface ProjectDao {

    /**
     * Thêm mới một dự án vào cơ sở dữ liệu.
     *
     * @param p Đối tượng [ProjectEntity] cần thêm mới.
     */
    @Insert
    suspend fun insert(p: ProjectEntity)

    /**
     * Lắng nghe danh sách các dự án thuộc một nhóm cụ thể theo thời gian thực,
     * kèm theo tổng số lượng công việc (task) của mỗi dự án.
     *
     * @param teamId Identity của nhóm cần lấy danh sách dự án.
     * @return Một [Flow] phát ra danh sách [ProjectWithCount] chứa thông tin tóm tắt và số lượng task của từng dự án.
     */
    @Query("""
        SELECT p.id, p.name, (SELECT COUNT(*) FROM tasks WHERE projectId = p.id) AS taskCount
        FROM projects p WHERE p.teamId = :teamId
    """)
    fun observeByTeam(teamId: String): Flow<List<ProjectWithCount>>
}
