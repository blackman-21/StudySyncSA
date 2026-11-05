package com.studysync.sa.data.remote

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.studysync.sa.data.local.entity.NoteEntity
import com.studysync.sa.data.local.entity.TaskEntity
import com.studysync.sa.data.local.entity.UserEntity
import kotlinx.coroutines.tasks.await

/**
 * Manages Firebase Realtime Database operations safely for StudySync SA.
 */
class RealtimeDbManager {

    private val tag = "RealtimeDbManager"
    private val database: FirebaseDatabase? by lazy {
        try {
            FirebaseDatabase.getInstance()
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize Firebase Realtime Database", e)
            null
        }
    }

    /**
     * Saves or updates a task in Firebase Realtime Database under '/tasks/{taskId}'.
     */
    suspend fun saveTask(task: TaskEntity): Boolean {
        val db = database ?: return false
        return try {
            val taskRef = db.getReference("tasks").child(task.id)
            taskRef.setValue(task).await()
            Log.d(tag, "Successfully saved task ${task.id} to Realtime Database")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to save task ${task.id} to Realtime Database", e)
            false
        }
    }

    /**
     * Saves or updates a note in Firebase Realtime Database under '/notes/{noteId}'.
     */
    suspend fun saveNote(note: NoteEntity): Boolean {
        val db = database ?: return false
        return try {
            val noteRef = db.getReference("notes").child(note.id)
            noteRef.setValue(note).await()
            Log.d(tag, "Successfully saved note ${note.id} to Realtime Database")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to save note ${note.id} to Realtime Database", e)
            false
        }
    }

    /**
     * Saves user profile in Firebase Realtime Database under '/users/{userId}'.
     */
    suspend fun saveUser(user: UserEntity): Boolean {
        val db = database ?: return false
        return try {
            val userRef = db.getReference("users").child(user.id)
            userRef.setValue(user).await()
            Log.d(tag, "Successfully saved user ${user.id} to Realtime Database")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to save user ${user.id} to Realtime Database", e)
            false
        }
    }

    /**
     * Listens for real-time task updates on node '/tasks'.
     */
    fun observeTasks(onDataChanged: (List<TaskEntity>) -> Unit) {
        val db = database ?: return
        val tasksRef = db.getReference("tasks")
        tasksRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasksList = snapshot.children.mapNotNull { child ->
                        child.getValue(TaskEntity::class.java)
                    }
                    onDataChanged(tasksList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(tag, "Realtime Database listener cancelled: ${error.message}")
                }
            },
        )
    }
}
