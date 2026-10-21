package com.example.taskmanager.util

import com.example.taskmanager.core.security.PasswordHasher
import org.junit.Assert.*
import org.junit.Test

class PasswordHasherTest {
    private val hasher = PasswordHasher()

    @Test
    fun hash_returnsValidHashString() {
        // Arrange
        val password = "MySecretPassword2232";

        // Act
        val hash = hasher.hash(password)

        // Assert
        assertNotNull(hash)
        assertNotEquals(password, hash)
    }

    @Test
    fun verify_withCorrectPassword_returnsTrue() {
        // Arrange
        val password = "MySecretPassword2232";
        val hash = hasher.hash(password)

        // Act
        val isValid = hasher.verify(password, hash)

        // Assert
        assertTrue(isValid)
    }

    @Test
    fun verify_withWrongPassword_returnsFalse() {
        // Arrange
        val password = "Password132@"
        val wrongPassword = "WrongPassword123"
        val hash = hasher.hash(password)

        // Act
        val isValid = hasher.verify(wrongPassword, hash)

        // Assert
        assertFalse(isValid)
    }
}