package com.studysync.sa.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studysync.sa.data.local.entity.TaskEntity
import com.studysync.sa.data.repository.StudySyncRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for managing Tasks.
 */
class TaskViewModel(private val repository: StudySyncRepository) : ViewModel() {

    val allTasks: StateFlow<List<TaskEntity>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(title: String, description: String, moduleId: String, dueAt: Long, priority: Int) {
        if (title.isBlank()) return

        val newTask = TaskEntity(
            id = UUID.randomUUID().toString(),
            moduleId = moduleId,
            title = title,
            description = description,
            dueAt = dueAt,
            priority = priority
        )
        viewModelScope.launch {
            repository.addTask(newTask)
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(completed = !task.completed))
        }
    }
}
