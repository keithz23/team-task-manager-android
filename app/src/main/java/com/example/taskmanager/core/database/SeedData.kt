package com.example.taskmanager.core.database

import androidx.room.withTransaction
import com.example.taskmanager.core.database.entity.CommentEntity
import com.example.taskmanager.core.database.entity.ProjectEntity
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.database.entity.TeamEntity
import com.example.taskmanager.core.database.entity.TeamMemberEntity
import com.example.taskmanager.core.database.entity.UserEntity
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.model.TeamRole
import com.example.taskmanager.core.security.PasswordHasher

/**
 * Dữ liệu mẫu với ID cố định, dùng chung cho test (DB in-memory) và DebugSeeder.
 *
 * Tài khoản: [EMAIL_AN] / [EMAIL_BINH], mật khẩu [PASSWORD].
 * An là OWNER của team, Bình là MEMBER. Project có 3 task (TODO/DOING/DONE),
 * [TASK_TODO] có sẵn 2 bình luận.
 */
object SeedData {
    const val USER_AN = "user-an"
    const val USER_BINH = "user-binh"
    const val EMAIL_AN = "an@demo.com"
    const val EMAIL_BINH = "binh@demo.com"
    const val PASSWORD = "123456"

    const val TEAM_ID = "team-demo"
    const val PROJECT_ID = "project-demo"

    const val TASK_TODO = "task-todo"
    const val TASK_DOING = "task-doing"
    const val TASK_DONE = "task-done"

    const val COMMENT_1 = "comment-1"
    const val COMMENT_2 = "comment-2"

    /** Mốc thời gian cố định để thứ tự (createdAt) luôn giống nhau giữa các lần chạy. */
    private const val T0 = 1_700_000_000_000L
    private const val MINUTE = 60_000L

    /**
     * Ghi dữ liệu mẫu vào [db]. Chạy trong một transaction: lỗi giữa chừng thì không ghi gì.
     * Thứ tự insert theo khóa ngoại: user → team → member → project → task → comment.
     * Chỉ gọi trên DB rỗng (ID cố định nên gọi lần 2 sẽ lỗi trùng khóa).
     */
    suspend fun seed(db: AppDatabase, hasher: PasswordHasher) = db.withTransaction {
        val passwordHash = hasher.hash(PASSWORD)
        db.userDao().insert(UserEntity(USER_AN, "Nguyễn An", EMAIL_AN, passwordHash, T0))
        db.userDao().insert(UserEntity(USER_BINH, "Trần Bình", EMAIL_BINH, passwordHash, T0))

        db.teamDao().createTeam(TeamEntity(TEAM_ID, "Team Demo", T0), ownerId = USER_AN)
        db.teamDao().insertMember(TeamMemberEntity(TEAM_ID, USER_BINH, TeamRole.MEMBER))

        db.projectDao().insert(ProjectEntity(PROJECT_ID, TEAM_ID, "App quản lý công việc"))

        db.taskDao().insert(
            TaskEntity(
                id = TASK_TODO, projectId = PROJECT_ID, title = "Thiết kế màn hình đăng nhập",
                description = "Làm theo mockup trên Figma", priority = Priority.HIGH,
                assigneeId = USER_AN, createdAt = T0,
            )
        )
        db.taskDao().insert(
            TaskEntity(
                id = TASK_DOING, projectId = PROJECT_ID, title = "Viết API tạo task",
                status = TaskStatus.DOING, assigneeId = USER_BINH, createdAt = T0 + MINUTE,
            )
        )
        db.taskDao().insert(
            TaskEntity(
                id = TASK_DONE, projectId = PROJECT_ID, title = "Khởi tạo project",
                status = TaskStatus.DONE, priority = Priority.LOW, createdAt = T0 + 2 * MINUTE,
            )
        )

        db.commentDao().insert(CommentEntity(COMMENT_1, TASK_TODO, USER_AN, "Mình bắt đầu làm nhé", T0 + 3 * MINUTE))
        db.commentDao().insert(CommentEntity(COMMENT_2, TASK_TODO, USER_BINH, "Ok, cần hỗ trợ cứ báo", T0 + 4 * MINUTE))
    }
}
