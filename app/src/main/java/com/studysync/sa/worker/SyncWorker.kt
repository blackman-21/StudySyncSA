package com.studysync.sa.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studysync.sa.data.local.StudySyncDatabase
import com.studysync.sa.data.local.entity.NoteEntity
import com.studysync.sa.data.local.entity.TaskEntity
import com.studysync.sa.data.local.entity.UserEntity
import com.studysync.sa.data.remote.NoSqlSyncManager
import com.studysync.sa.data.remote.RealtimeDbManager
import com.studysync.sa.data.remote.StudySyncApiService
import com.studysync.sa.data.remote.SyncRequest
import com.studysync.sa.notification.LocalNotificationManager
import androidx.room.Room
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

/**
 * Background worker that synchronizes pending local changes with the remote REST API, Cloud Firestore, and Firebase Realtime Database.
 */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    private val tag = "SyncWorker"
    private val noSqlSyncManager by lazy { NoSqlSyncManager() }
    private val realtimeDbManager by lazy { RealtimeDbManager() }
    private val notificationManager by lazy { LocalNotificationManager(applicationContext) }

    private val database by lazy {
        Room.databaseBuilder(
            appContext,
            StudySyncDatabase::class.java,
            "studysync_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    private val apiService by lazy {
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(StudySyncApiService::class.java)
    }

    private val jsonFormatter = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result {
        Log.d(tag, "Starting offline sync process...")
        val dao = database.studySyncDao()
        val pendingOperations = dao.getPendingSyncOperations()

        if (pendingOperations.isEmpty()) {
            Log.d(tag, "No pending operations to sync.")
            return Result.success()
        }

        var allSuccessful = true

        for (op in pendingOperations) {
            try {
                // 1. Sync with REST API
                val request = SyncRequest(
                    entityId = op.entityId,
                    entityType = op.entityType,
                    operationType = op.operationType,
                    payload = op.payload
                )

                val response = apiService.syncOperation(request)

                // 2. Sync with Cloud Firestore & Firebase Realtime Database
                when (op.entityType) {
                    "TASK" -> {
                        try {
                            val task = jsonFormatter.decodeFromString<TaskEntity>(op.payload)
                            noSqlSyncManager.saveTask(task)
                            realtimeDbManager.saveTask(task)
                        } catch (e: Exception) {
                            Log.w(tag, "Task sync exception", e)
                        }
                    }
                    "NOTE" -> {
                        try {
                            val note = jsonFormatter.decodeFromString<NoteEntity>(op.payload)
                            noSqlSyncManager.saveNote(note)
                            realtimeDbManager.saveNote(note)
                        } catch (e: Exception) {
                            Log.w(tag, "Note sync exception", e)
                        }
                    }
                    "USER" -> {
                        try {
                            val user = jsonFormatter.decodeFromString<UserEntity>(op.payload)
                            realtimeDbManager.saveUser(user)
                        } catch (e: Exception) {
                            Log.w(tag, "User sync exception", e)
                        }
                    }
                }

                if (response.isSuccessful) {
                    dao.updateSyncOperation(op.copy(status = "SYNCED"))
                    Log.d(tag, "Successfully synced operation: ${op.id}")
                } else {
                    dao.updateSyncOperation(op.copy(status = "FAILED"))
                    allSuccessful = false
                }
            } catch (e: Exception) {
                Log.e(tag, "Error syncing operation ${op.id}", e)
                dao.updateSyncOperation(op.copy(status = "FAILED"))
                allSuccessful = false
            }
        }

        if (allSuccessful) {
            notificationManager.showNotification(
                notificationId = 101,
                title = "Data Synchronized",
                message = "All offline study changes have been synced with Firebase Realtime Database & Cloud.",
                channelId = LocalNotificationManager.CHANNEL_ID_SYNC
            )
        }

        return if (allSuccessful) Result.success() else Result.retry()
    }
}
