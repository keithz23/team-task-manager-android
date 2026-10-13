package com.example.taskmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.taskmanager.core.database.entity.TeamEntity
import com.example.taskmanager.core.database.entity.TeamMemberEntity
import com.example.taskmanager.core.database.relation.MemberWithUser
import com.example.taskmanager.core.database.relation.TeamWithCounts
import com.example.taskmanager.core.model.TeamRole
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) cung cấp các phương thức thao tác với bảng `teams` và `team_members` trong cơ sở dữ liệu Room.
 */
@Dao
interface TeamDao {

    /**
     * Thêm mới một nhóm vào cơ sở dữ liệu.
     *
     * @param t Đối tượng [TeamEntity] cần thêm mới.
     */
    @Insert
    suspend fun insertTeam(t: TeamEntity)

    /**
     * Thêm mới một bản ghi thành viên nhóm vào cơ sở dữ liệu.
     *
     * @param m Thực thể [TeamMemberEntity] cần thêm mới.
     */
    @Insert
    suspend fun insertMember(m: TeamMemberEntity)

    /**
     * Tạo mới một nhóm và tự động gán vai trò chủ sở hữu ([TeamRole.OWNER]) cho người dùng tạo nhóm.
     * Phương thức này thực thi trong một giao dịch cơ sở dữ liệu ([Transaction]).
     *
     * @param team Đối tượng [TeamEntity] của nhóm cần tạo.
     * @param ownerId Identity của người dùng tạo nhóm (sẽ nhận vai trò OWNER).
     */
    @Transaction
    suspend fun createTeam(team: TeamEntity, ownerId: String) {
        insertTeam(team)
        insertMember(TeamMemberEntity(team.id, ownerId, TeamRole.OWNER))
    }

    /**
     * Lắng nghe danh sách các nhóm mà người dùng tham gia theo thời gian thực,
     * kèm theo thông tin vai trò và tổng số thành viên của mỗi nhóm.
     *
     * @param userId Identity của người dùng cần lấy danh sách nhóm.
     * @return Một [Flow] phát ra danh sách [TeamWithCounts] được sắp xếp theo thời gian tạo giảm dần.
     */
    @Query("""
        SELECT t.id, t.name, tm.role,
               (SELECT COUNT(*) FROM team_members WHERE teamId = t.id) AS memberCount
        FROM teams t JOIN team_members tm ON tm.teamId = t.id
        WHERE tm.userId = :userId ORDER BY t.createdAt DESC
    """)
    fun observeTeamsOf(userId: String): Flow<List<TeamWithCounts>>

    /**
     * Lắng nghe danh sách thành viên thuộc một nhóm cụ thể theo thời gian thực,
     * kèm theo thông tin chi tiết của từng người dùng.
     *
     * @param teamId Identity của nhóm cần quan sát thành viên.
     * @return Một [Flow] phát ra danh sách [MemberWithUser] chứa thông tin thành viên và người dùng tương ứng.
     */
    @Transaction
    @Query("SELECT * FROM team_members WHERE teamId = :teamId")
    fun observeMembers(teamId: String): Flow<List<MemberWithUser>>

    /**
     * Lấy vai trò của một người dùng cụ thể trong một nhóm nhất định.
     *
     * @param teamId Identity của nhóm.
     * @param userId Identity của người dùng.
     * @return Vai trò [TeamRole] của người dùng trong nhóm, hoặc `null` nếu người dùng không thuộc nhóm đó.
     */
    @Query("SELECT role FROM team_members WHERE teamId = :teamId AND userId = :userId")
    suspend fun roleOf(teamId: String, userId: String): TeamRole?
}
