package com.studysync.sa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_operations")
data class SyncOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityId: String,
    val entityType: String, // TASK, NOTE, MODULE
    val operationType: String, // CREATE, UPDATE, DELETE
    val payload: String, // JSON representation of the entity
    val status: String, // PENDING, SYNCED, FAILED
    val timestamp: Long = System.currentTimeMillis()
)
