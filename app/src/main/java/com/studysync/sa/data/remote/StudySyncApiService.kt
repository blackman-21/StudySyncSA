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
    val success: Boolean = true,
    val message: String? = null
)

interface StudySyncApiService {
    // Connected to a reliable live sandbox placeholder endpoint to simulate sync events safely
    @POST("posts")
    suspend fun syncOperation(@Body request: SyncRequest): Response<SyncResponse>
}
