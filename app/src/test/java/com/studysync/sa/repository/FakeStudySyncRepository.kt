package com.studysync.sa.repository

import com.studysync.sa.data.local.entity.NoteEntity
import com.studysync.sa.data.local.entity.TaskEntity
import com.studysync.sa.data.local.entity.UserEntity
import com.studysync.sa.data.repository.StudySyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Pure Kotlin in-memory Fake implementation of StudySyncRepository for fast, zero-dependency unit testing.
 */
class FakeStudySyncRepository : StudySyncRepository(context = null, dao = null) {

    private val tasksFlow = MutableStateFlow<List<TaskEntity>>(emptyList())
    private val notesFlow = MutableStateFlow<List<NoteEntity>>(emptyList())
    private val usersList = mutableListOf<UserEntity>()

    var addTaskCalled = false
    var updateTaskCalled = false
    var addNoteCalled = false
    var registerUserCalled = false

    override fun getAllTasks(): Flow<List<TaskEntity>> = tasksFlow.asStateFlow()

    override suspend fun addTask(task: TaskEntity) {
        addTaskCalled = true
        tasksFlow.value = tasksFlow.value + task
    }

    override suspend fun updateTask(task: TaskEntity) {
        updateTaskCalled = true
        tasksFlow.value = tasksFlow.value.map { if (it.id == task.id) task else it }
    }

    override fun getAllNotes(): Flow<List<NoteEntity>> = notesFlow.asStateFlow()

    override suspend fun addNote(note: NoteEntity) {
        addNoteCalled = true
        notesFlow.value = notesFlow.value + note
    }

    override suspend fun registerUser(user: UserEntity) {
        registerUserCalled = true
        usersList.add(user)
    }

    override suspend fun getUserByEmail(email: String): UserEntity? {
        return usersList.find { it.email == email }
    }

    override fun scheduleSync() {
        // No-op for unit tests
    }
}
