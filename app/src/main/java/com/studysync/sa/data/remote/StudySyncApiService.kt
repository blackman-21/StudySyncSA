package com.studysync.sa.data.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class SyncRequest(
    val entityId: String,
    val entityType: String,
    val operationType: String,
    val payload: String
)

@Serializable
data class SyncResponse(
    val success: Boolean,
    val message: String? = null
)

interface StudySyncApiService {
    @POST("api/v1/sync")
    suspend fun syncOperation(@Body request: SyncRequest): Response<SyncResponse>
}
