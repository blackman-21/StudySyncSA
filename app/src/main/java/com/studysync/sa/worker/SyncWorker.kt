package com.studysync.sa.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studysync.sa.data.local.StudySyncDatabase
import com.studysync.sa.data.remote.StudySyncApiService
import com.studysync.sa.data.remote.SyncRequest
import androidx.room.Room
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

/**
 * Background worker that synchronizes pending local changes with the remote server.
 */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val TAG = "SyncWorker"

    // In a real app, use Dependency Injection (like Hilt) to provide these
    private val database by lazy {
        Room.databaseBuilder(
            appContext,
            StudySyncDatabase::class.java,
            "studysync_db"
        ).build()
    }

    private val apiService by lazy {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl("https://api.studysyncsa.example.com/") // Placeholder URL
            .addConverterFactory(Json.asConverterFactory(contentType))
            .build()
            .create(StudySyncApiService::class.java)
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting sync process...")
        val dao = database.studySyncDao()
        val pendingOperations = dao.getPendingSyncOperations()

        if (pendingOperations.isEmpty()) {
            Log.d(TAG, "No pending operations to sync.")
            return Result.success()
        }

        var allSuccessful = true

        for (op in pendingOperations) {
            try {
                val request = SyncRequest(
                    entityId = op.entityId,
                    entityType = op.entityType,
                    operationType = op.operationType,
                    payload = op.payload
                )

                val response = apiService.syncOperation(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    dao.updateSyncOperation(op.copy(status = "SYNCED"))
                    Log.d(TAG, "Successfully synced operation: ${op.id}")
                } else {
                    dao.updateSyncOperation(op.copy(status = "FAILED"))
                    Log.e(TAG, "Failed to sync operation ${op.id}: ${response.message()}")
                    allSuccessful = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing operation ${op.id}", e)
                dao.updateSyncOperation(op.copy(status = "FAILED"))
                allSuccessful = false
            }
        }

        return if (allSuccessful) Result.success() else Result.retry()
    }
}
