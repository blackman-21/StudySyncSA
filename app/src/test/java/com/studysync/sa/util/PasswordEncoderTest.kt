package com.studysync.sa.util

import org.junit.Assert.*
import org.junit.Test

class PasswordEncoderTest {

    @Test
    fun `generateSalt returns non-empty string`() {
        val salt = PasswordEncoder.generateSalt()
        assertNotNull(salt)
        assertTrue(salt.isNotBlank())
    }

    @Test
    fun `hashPassword produces consistent hash for same password and salt`() {
        val password = "MySecurePassword123!"
        val salt = "random_salt_12345"

        val hash1 = PasswordEncoder.hashPassword(password, salt)
        val hash2 = PasswordEncoder.hashPassword(password, salt)

        assertEquals(hash1, hash2)
        assertNotEquals(password, hash1)
    }

    @Test
    fun `verifyPassword returns true for correct credentials`() {
        val password = "SecretPassword"
        val salt = PasswordEncoder.generateSalt()
        val hash = PasswordEncoder.hashPassword(password, salt)

        assertTrue(PasswordEncoder.verifyPassword(password, salt, hash))
        assertFalse(PasswordEncoder.verifyPassword("WrongPassword", salt, hash))
    }

    @Test
    fun `isValidEmail correctly validates emails`() {
        assertTrue(PasswordEncoder.isValidEmail("student@university.ac.za"))
        assertTrue(PasswordEncoder.isValidEmail("john.doe@gmail.com"))
        assertFalse(PasswordEncoder.isValidEmail("invalid-email"))
        assertFalse(PasswordEncoder.isValidEmail(""))
    }

    @Test
    fun `isValidPassword checks password length`() {
        assertTrue(PasswordEncoder.isValidPassword("123456"))
        assertFalse(PasswordEncoder.isValidPassword("12345"))
    }
}
