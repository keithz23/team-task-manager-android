package com.example.taskmanager.core.database

import android.util.Log
import com.example.taskmanager.core.security.PasswordHasher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Nạp [SeedData] vào DB thật khi chạy bản debug, để mở app lên là đăng nhập được ngay
 * (an@demo.com / 123456) và có sẵn team, project, task, bình luận để demo.
 * Gọi từ TaskApp; bản release không gọi.
 */
@Singleton
class DebugSeeder @Inject constructor(
    private val db: AppDatabase,
    private val hasher: PasswordHasher,
) {
    /** Chỉ seed khi chưa có tài khoản demo, nên gọi nhiều lần (mỗi lần mở app) cũng không sao. */
    suspend fun seedIfNeeded() {
        if (db.userDao().findById(SeedData.USER_AN) != null) return
        // Lỗi (vd. email demo đã bị người khác đăng ký) chỉ ghi log, không làm app crash.
        runCatching { SeedData.seed(db, hasher) }
            .onFailure { Log.w("DebugSeeder", "Không seed được dữ liệu demo", it) }
    }
}
