package com.studysync.sa.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Manages Blob Storage operations (Firebase Cloud Storage & local persistent media storage).
 * Enables uploading note image attachments, study material documents, and user profile pictures.
 */
class BlobStorageManager(private val context: Context) {
    private val TAG = "BlobStorageManager"
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    /**
     * Uploads a byte array blob to cloud storage under the specified path.
     * Returns the publicly accessible blob URL on success.
     */
    suspend fun uploadBlob(blobPath: String, data: ByteArray): String? {
        return try {
            val storageRef = storage.reference.child(blobPath)
            val uploadTask = storageRef.putBytes(data).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            Log.d(TAG, "Blob uploaded successfully: ${downloadUrl}")
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload blob to cloud storage. Fallback to local storage path.", e)
            // Local fallback store for offline / un-configured Firebase environments
            saveLocalBlob(blobPath.replace("/", "_"), data)
        }
    }

    /**
     * Helper to convert Bitmap image to JPEG ByteArray for blob upload.
     */
    fun bitmapToBytes(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }

    /**
     * Saves a blob locally in app private storage when offline.
     */
    fun saveLocalBlob(fileName: String, data: ByteArray): String {
        return try {
            val file = File(context.filesDir, fileName)
            file.writeBytes(data)
            Log.d(TAG, "Local blob stored at: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write local blob", e)
            ""
        }
    }
}
