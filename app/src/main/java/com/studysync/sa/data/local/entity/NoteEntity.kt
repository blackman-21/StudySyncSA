package com.studysync.sa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val moduleId: String,
    val title: String,
    val body: String,
    val attachmentUrl: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
