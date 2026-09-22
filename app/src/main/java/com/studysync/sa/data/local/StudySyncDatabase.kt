package com.studysync.sa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.studysync.sa.data.local.dao.StudySyncDao
import com.studysync.sa.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        ModuleEntity::class,
        TaskEntity::class,
        NoteEntity::class,
        SyncOperationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StudySyncDatabase : RoomDatabase() {
    abstract fun studySyncDao(): StudySyncDao
}
