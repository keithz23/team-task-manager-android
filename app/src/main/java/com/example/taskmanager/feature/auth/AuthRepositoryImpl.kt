package com.example.taskmanager.feature.auth

import android.database.sqlite.SQLiteConstraintException
import com.example.taskmanager.core.database.dao.UserDao
import com.example.taskmanager.core.database.entity.UserEntity
import com.example.taskmanager.core.security.PasswordHasher
import com.example.taskmanager.core.session.AuthState
import com.example.taskmanager.core.session.SessionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

class AuthException(message: String) : Exception(message)

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val hasher: PasswordHasher,
    private val session: SessionManager,
) : AuthRepository {

    override val currentUserId: Flow<String?> = session.authState.map { state ->
        (state as? AuthState.LoggedIn)?.userId
    }

    override suspend fun register(name: String, email: String, password: String): Result<Unit> =
        attempt {
            val cleanName = name.trim()
            val cleanEmail = email.normalized()

            if (cleanName.isEmpty()) throw AuthException("Họ tên không được để trống")
            if (!EMAIL_REGEX.matches(cleanEmail)) throw AuthException("Email không hợp lệ")
            if (password.length < MIN_PASSWORD) {
                throw AuthException("Mật khẩu tối thiểu $MIN_PASSWORD ký tự")
            }
            if (userDao.findByEmail(cleanEmail) != null) {
                throw AuthException("Email đã được sử dụng")
            }

            val hash = withContext(Dispatchers.Default) { hasher.hash(password) }
            val user = UserEntity(name = cleanName, email = cleanEmail, passwordHash = hash)

            try {
                userDao.insert(user)
            } catch (e: SQLiteConstraintException) {
                throw AuthException("Email đã được sử dụng")   // trùng do đăng ký đồng thời
            }
            session.save(user.id)   // đăng ký xong vào luôn app
        }

    override suspend fun login(email: String, password: String): Result<Unit> =
        attempt {
            val cleanEmail = email.normalized()
            if (cleanEmail.isEmpty() || password.isEmpty()) {
                throw AuthException("Vui lòng nhập email và mật khẩu")
            }

            val user = userDao.findByEmail(cleanEmail)
            val ok = user != null &&
                    withContext(Dispatchers.Default) { hasher.verify(password, user.passwordHash) }
            if (!ok) throw AuthException("Email hoặc mật khẩu không đúng")

            session.save(user.id)
        }

    override suspend fun getUser(userId: String): Result<UserEntity> =
        attempt {
            userDao.findById(userId) ?: throw AuthException("Không tìm thấy người dùng")
        }

    override suspend fun logout() = session.clear()

    private fun String.normalized() = trim().lowercase()

    /**
     * Như runCatching nhưng:
     * - không nuốt CancellationException (để coroutine huỷ được)
     * - giữ nguyên AuthException (đã có message tiếng Việt)
     * - lỗi bất ngờ khác đổi thành thông báo thân thiện, không lộ chuỗi kỹ thuật ra UI
     */
    private inline fun <T> attempt(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: AuthException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthException("Đã có lỗi xảy ra, vui lòng thử lại"))
        }

    private companion object {
        const val MIN_PASSWORD = 6
        val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}