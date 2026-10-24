package com.example.taskmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.taskmanager.core.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) cung cấp các phương thức thao tác với bảng `users` trong cơ sở dữ liệu Room.
 */
@Dao
interface UserDao {

    /**
     * Thêm mới một người dùng vào cơ sở dữ liệu.
     *
     * @param u Thực thể [UserEntity] cần thêm mới.
     * @throws android.database.sqlite.SQLiteConstraintException Nếu email đã tồn tại (do chiến lược [OnConflictStrategy.ABORT]).
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(u: UserEntity)

    /**
     * Tìm kiếm thông tin người dùng dựa trên địa chỉ email.
     *
     * @param email Địa chỉ email cần tìm.
     * @return Đối tượng [UserEntity] nếu tìm thấy, hoặc `null` nếu không tồn tại.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    /**
     * Tìm kiếm thông tin người dùng dựa trên ID.
     *
     * @param id Định danh của người dùng cần tìm.
     * @return Đối tượng [UserEntity] nếu tìm thấy, hoặc `null` nếu không tồn tại.
     */
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): UserEntity?

    /**
     * Lắng nghe các thay đổi về thông tin của người dùng theo thời gian thực (reactive stream).
     *
     * @param id Định danh duy nhất của người dùng cần quan sát.
     * @return Một [Flow] phát ra đối tượng [UserEntity?] mỗi khi dữ liệu của người dùng này thay đổi trong cơ sở dữ liệu.
     */
    @Query("SELECT * FROM users WHERE id = :id")
    fun observe(id: String): Flow<UserEntity?>
}
