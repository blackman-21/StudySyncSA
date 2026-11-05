package com.studysync.sa.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.regex.Pattern

/**
 * Utility for hashing and validating passwords using SHA-256 with a salt,
 * ensuring sensitive user credentials are never saved in plaintext.
 */
object PasswordEncoder {

    private const val ALGORITHM = "SHA-256"

    /**
     * Generates a random 16-byte salt for password hashing.
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return Base64.getEncoder().encodeToString(saltBytes)
    }

    /**
     * Hashes a raw password with the provided salt using SHA-256.
     */
    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance(ALGORITHM)
        val combined = password + salt
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    /**
     * Verifies if a raw password matches a previously hashed password and salt.
     */
    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val computedHash = hashPassword(password, salt)
        return computedHash == expectedHash
    }

    /**
     * Validates email string format.
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return emailPattern.matcher(email.trim()).matches()
    }

    /**
     * Validates password strength (at least 6 characters).
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}
