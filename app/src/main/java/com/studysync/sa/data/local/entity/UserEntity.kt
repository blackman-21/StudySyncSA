package com.studysync.sa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val surname: String = "",
    val email: String,
    val passwordHash: String = "",
    val salt: String = "",
    val profilePictureUrl: String? = null,
    val language: String = "en", // en, zu, af, xh
    val notificationsEnabled: Boolean = true
)
