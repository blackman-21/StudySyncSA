package com.studysync.sa.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.studysync.sa.data.local.entity.NoteEntity
import com.studysync.sa.data.local.entity.TaskEntity
import kotlinx.coroutines.tasks.await

/**
 * Manages NoSQL database operations (Cloud Firestore document/collection store).
 * Provides methods to store and read app data (Tasks, Notes, User Profile) from NoSQL.
 */
class NoSqlSyncManager {
    private val tag = "NoSqlSyncManager"
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize Firebase Firestore", e)
            null
        }
    }

    /**
     * Stores a task document in NoSQL Firestore database under collection 'tasks'.
     */
    suspend fun saveTask(task: TaskEntity): Boolean {
        val fs = firestore ?: return false
        return try {
            val taskMap = hashMapOf(
                "id" to task.id,
                "moduleId" to task.moduleId,
                "title" to task.title,
                "description" to task.description,
                "dueAt" to task.dueAt,
                "priority" to task.priority,
                "completed" to task.completed,
                "updatedAt" to System.currentTimeMillis()
            )
            fs.collection("tasks").document(task.id)
                .set(taskMap, SetOptions.merge()).await()
            Log.d(tag, "Successfully saved task ${task.id} to NoSQL DB")
            true
        } catch (e: Exception) {
            Log.e(tag, "Error saving task ${task.id} to NoSQL DB", e)
            false
        }
    }

    /**
     * Reads all task documents from NoSQL Firestore database.
     */
    suspend fun fetchTasks(): List<TaskEntity> {
        val fs = firestore ?: return emptyList()
        return try {
            val snapshot = fs.collection("tasks").get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val moduleId = doc.getString("moduleId") ?: ""
                val title = doc.getString("title") ?: ""
                val description = doc.getString("description") ?: ""
                val dueAt = doc.getLong("dueAt") ?: System.currentTimeMillis()
                val priority = (doc.getLong("priority") ?: 1L).toInt()
                val completed = doc.getBoolean("completed") ?: false
                TaskEntity(id, moduleId, title, description, dueAt, priority, completed)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching tasks from NoSQL DB", e)
            emptyList()
        }
    }

    /**
     * Stores a note document in NoSQL Firestore database under collection 'notes'.
     */
    suspend fun saveNote(note: NoteEntity): Boolean {
        val fs = firestore ?: return false
        return try {
            val noteMap = hashMapOf(
                "id" to note.id,
                "moduleId" to note.moduleId,
                "title" to note.title,
                "body" to note.body,
                "attachmentUrl" to note.attachmentUrl,
                "updatedAt" to note.updatedAt
            )
            fs.collection("notes").document(note.id)
                .set(noteMap, SetOptions.merge()).await()
            Log.d(tag, "Successfully saved note ${note.id} to NoSQL DB")
            true
        } catch (e: Exception) {
            Log.e(tag, "Error saving note ${note.id} to NoSQL DB", e)
            false
        }
    }

    /**
     * Reads all note documents from NoSQL Firestore database.
     */
    suspend fun fetchNotes(): List<NoteEntity> {
        val fs = firestore ?: return emptyList()
        return try {
            val snapshot = fs.collection("notes").get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val moduleId = doc.getString("moduleId") ?: ""
                val title = doc.getString("title") ?: ""
                val body = doc.getString("body") ?: ""
                val attachmentUrl = doc.getString("attachmentUrl")
                val updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                NoteEntity(
                    id = id,
                    moduleId = moduleId,
                    title = title,
                    body = body,
                    attachmentUrl = attachmentUrl,
                    updatedAt = updatedAt
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching notes from NoSQL DB", e)
            emptyList()
        }
    }
}
