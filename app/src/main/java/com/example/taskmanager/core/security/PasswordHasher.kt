package com.example.taskmanager.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/** Hash mật khẩu bằng PBKDF2-HMAC-SHA256. Định dạng lưu: `iterations$salt$hash` (Base64). */
@Singleton
class PasswordHasher @Inject constructor() {

    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = pbkdf2(password.toCharArray(), salt, ITERATIONS)
        return "$ITERATIONS$$SEP${b64(salt)}$SEP${b64(hash)}".replace("$$", "$")
    }

    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split(SEP)
        if (parts.size != 3) return false
        val iterations = parts[0].toIntOrNull() ?: return false
        val salt = runCatching { Base64.getDecoder().decode(parts[1]) }.getOrNull() ?: return false
        val expected = runCatching { Base64.getDecoder().decode(parts[2]) }.getOrNull() ?: return false
        val actual = pbkdf2(password.toCharArray(), salt, iterations)
        return MessageDigest.isEqual(expected, actual)   // so sánh thời gian không đổi
    }

    private fun pbkdf2(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun b64(bytes: ByteArray) = Base64.getEncoder().encodeToString(bytes)

    private companion object {
        const val SEP = "$"
        const val SALT_BYTES = 16
        const val KEY_BITS = 256
        const val ITERATIONS = 120_000
    }
}
