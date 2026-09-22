package com.studysync.sa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val language: String, // en, xh, af
    val notificationsEnabled: Boolean = true
)
