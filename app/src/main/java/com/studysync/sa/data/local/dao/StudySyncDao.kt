package com.studysync.sa.data.local.dao

import androidx.room.*
import com.studysync.sa.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySyncDao {
    // User
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    // Modules
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: ModuleEntity)

    @Query("SELECT * FROM modules")
    fun getAllModules(): Flow<List<ModuleEntity>>

    // Tasks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE moduleId = :moduleId")
    fun getTasksByModule(moduleId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // Notes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("SELECT * FROM notes")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%'")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    // Sync Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncOperation(operation: SyncOperationEntity)

    @Query("SELECT * FROM sync_operations WHERE status = 'PENDING' ORDER BY timestamp ASC")
    suspend fun getPendingSyncOperations(): List<SyncOperationEntity>

    @Update
    suspend fun updateSyncOperation(operation: SyncOperationEntity)
}
