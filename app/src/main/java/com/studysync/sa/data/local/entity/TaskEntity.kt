package com.studysync.sa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val moduleId: String,
    val title: String,
    val description: String,
    val dueAt: Long,
    val priority: Int, // 1-4
    val completed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
