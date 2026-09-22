package com.studysync.sa.data.repository

import android.content.Context
import android.util.Log
import androidx.work.*
import com.studysync.sa.data.local.dao.StudySyncDao
import com.studysync.sa.data.local.entity.*
import com.studysync.sa.worker.SyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

/**
 * Repository responsible for data operations, implementing an offline-first strategy.
 */
class StudySyncRepository(
    private val context: Context,
    private val dao: StudySyncDao
) {
    private val TAG = "StudySyncRepository"

    // Modules
    fun getAllModules(): Flow<List<ModuleEntity>> = dao.getAllModules()

    suspend fun addModule(module: ModuleEntity) {
        try {
            dao.insertModule(module)
            recordSyncOperation(module.id, "MODULE", "CREATE", Json.encodeToString(module))
        } catch (e: Exception) {
            Log.e(TAG, "Error adding module", e)
        }
    }

    // Tasks
    fun getAllTasks(): Flow<List<TaskEntity>> = dao.getAllTasks()
    
    fun getTasksByModule(moduleId: String): Flow<List<TaskEntity>> = dao.getTasksByModule(moduleId)

    suspend fun addTask(task: TaskEntity) {
        try {
            dao.insertTask(task)
            recordSyncOperation(task.id, "TASK", "CREATE", Json.encodeToString(task))
        } catch (e: Exception) {
            Log.e(TAG, "Error adding task", e)
        }
    }

    suspend fun updateTask(task: TaskEntity) {
        try {
            dao.updateTask(task)
            recordSyncOperation(task.id, "TASK", "UPDATE", Json.encodeToString(task))
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task", e)
        }
    }

    // Notes
    fun getAllNotes(): Flow<List<NoteEntity>> = dao.getAllNotes()
    
    fun searchNotes(query: String): Flow<List<NoteEntity>> = dao.searchNotes(query)

    suspend fun addNote(note: NoteEntity) {
        try {
            dao.insertNote(note)
            recordSyncOperation(note.id, "NOTE", "CREATE", Json.encodeToString(note))
        } catch (e: Exception) {
            Log.e(TAG, "Error adding note", e)
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
        dao.insertSyncOperation(operation)
        Log.d(TAG, "Sync operation recorded: $operationType for $entityType")
        
        scheduleSync()
    }

    fun scheduleSync() {
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
    }
}
