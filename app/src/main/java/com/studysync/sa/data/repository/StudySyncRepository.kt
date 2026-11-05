package com.studysync.sa.data.repository

import android.content.Context
import android.util.Log
import androidx.work.*
import com.studysync.sa.data.local.dao.StudySyncDao
import com.studysync.sa.data.local.entity.*
import com.studysync.sa.data.remote.RealtimeDbManager
import com.studysync.sa.worker.SyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

/**
 * Repository responsible for data operations, implementing an offline-first strategy with Room and SyncWorker.
 */
open class StudySyncRepository(
    private val context: Context? = null,
    private val dao: StudySyncDao? = null
) {
    private val tag = "StudySyncRepository"
    private val realtimeDbManager by lazy { RealtimeDbManager() }

    // User Operations
    open suspend fun registerUser(user: UserEntity) {
        try {
            dao?.insertUser(user)
            recordSyncOperation(user.id, "USER", "CREATE", Json.encodeToString(user))
            try {
                realtimeDbManager.saveUser(user)
            } catch (e: Exception) {
                Log.w(tag, "Realtime DB registration sync exception", e)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error registering user", e)
        }
    }

    open suspend fun getUserByEmail(email: String): UserEntity? {
        return try {
            dao?.getUserByEmail(email.trim().lowercase())
        } catch (e: Exception) {
            Log.e(tag, "Error fetching user by email", e)
            null
        }
    }

    open suspend fun updateUser(user: UserEntity) {
        try {
            dao?.updateUser(user)
            recordSyncOperation(user.id, "USER", "UPDATE", Json.encodeToString(user))
            try {
                realtimeDbManager.saveUser(user)
            } catch (e: Exception) {
                Log.w(tag, "Realtime DB update user exception", e)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating user", e)
        }
    }

    // Modules
    open fun getAllModules(): Flow<List<ModuleEntity>> = dao?.getAllModules() ?: emptyFlow()

    open suspend fun addModule(module: ModuleEntity) {
        try {
            dao?.insertModule(module)
            recordSyncOperation(module.id, "MODULE", "CREATE", Json.encodeToString(module))
        } catch (e: Exception) {
            Log.e(tag, "Error adding module", e)
        }
    }

    // Tasks
    open fun getAllTasks(): Flow<List<TaskEntity>> = dao?.getAllTasks() ?: emptyFlow()
    
    open fun getTasksByModule(moduleId: String): Flow<List<TaskEntity>> = dao?.getTasksByModule(moduleId) ?: emptyFlow()

    open suspend fun addTask(task: TaskEntity) {
        try {
            dao?.insertTask(task)
            recordSyncOperation(task.id, "TASK", "CREATE", Json.encodeToString(task))
        } catch (e: Exception) {
            Log.e(tag, "Error adding task", e)
        }
    }

    open suspend fun updateTask(task: TaskEntity) {
        try {
            dao?.updateTask(task)
            recordSyncOperation(task.id, "TASK", "UPDATE", Json.encodeToString(task))
        } catch (e: Exception) {
            Log.e(tag, "Error updating task", e)
        }
    }

    open suspend fun deleteTask(task: TaskEntity) {
        try {
            dao?.deleteTask(task)
            recordSyncOperation(task.id, "TASK", "DELETE", Json.encodeToString(task))
        } catch (e: Exception) {
            Log.e(tag, "Error deleting task", e)
        }
    }

    // Notes
    open fun getAllNotes(): Flow<List<NoteEntity>> = dao?.getAllNotes() ?: emptyFlow()
    
    open fun searchNotes(query: String): Flow<List<NoteEntity>> = dao?.searchNotes(query) ?: emptyFlow()

    open suspend fun addNote(note: NoteEntity) {
        try {
            dao?.insertNote(note)
            recordSyncOperation(note.id, "NOTE", "CREATE", Json.encodeToString(note))
        } catch (e: Exception) {
            Log.e(tag, "Error adding note", e)
        }
    }

    open suspend fun deleteNote(note: NoteEntity) {
        try {
            dao?.deleteNote(note)
            recordSyncOperation(note.id, "NOTE", "DELETE", Json.encodeToString(note))
        } catch (e: Exception) {
            Log.e(tag, "Error deleting note", e)
        }
    }

    /**
     * Records a pending sync operation and schedules the SyncWorker.
     */
    private suspend fun recordSyncOperation(entityId: String, entityType: String, operationType: String, payload: String) {
        val operation = SyncOperationEntity(
            entityId = entityId,
            entityType = entityType,
            operationType = operationType,
            payload = payload,
            status = "PENDING"
        )
        dao?.insertSyncOperation(operation)
        Log.d(tag, "Sync operation recorded: $operationType for $entityType")
        
        scheduleSync()
    }

    open fun scheduleSync() {
        if (context == null) return
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "SyncWork",
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed to schedule SyncWorker", e)
        }
    }
}
