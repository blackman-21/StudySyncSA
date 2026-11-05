package com.studysync.sa.viewmodel

import com.studysync.sa.data.local.entity.TaskEntity
import com.studysync.sa.repository.FakeStudySyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    private lateinit var repository: FakeStudySyncRepository
    private lateinit var viewModel: TaskViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeStudySyncRepository()
        viewModel = TaskViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addTask with valid title calls repository addTask`() = runTest {
        viewModel.addTask("Study Mobile Dev", "Complete Part 3 PoE", "module_1", System.currentTimeMillis(), 1)
        assertTrue(repository.addTaskCalled)
        assertEquals(1, viewModel.allTasks.value.size)
        assertEquals("Study Mobile Dev", viewModel.allTasks.value.first().title)
    }

    @Test
    fun `addTask with blank title does not call repository`() = runTest {
        viewModel.addTask("", "Description", "module_1", 0L, 1)
        assertFalse(repository.addTaskCalled)
        assertTrue(viewModel.allTasks.value.isEmpty())
    }

    @Test
    fun `toggleTaskCompletion toggles completed status and calls repository`() = runTest {
        val task = TaskEntity("1", "mod1", "Title", "Desc", 0L, 1, false)
        repository.addTask(task)

        viewModel.toggleTaskCompletion(task)

        assertTrue(repository.updateTaskCalled)
        assertTrue(viewModel.allTasks.value.first().completed)
    }
}
