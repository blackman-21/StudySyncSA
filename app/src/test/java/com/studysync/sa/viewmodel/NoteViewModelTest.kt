package com.studysync.sa.viewmodel

import com.studysync.sa.repository.FakeStudySyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModelTest {

    private lateinit var repository: FakeStudySyncRepository
    private lateinit var viewModel: NoteViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeStudySyncRepository()
        viewModel = NoteViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addNote with valid inputs adds note to repository`() = runTest {
        val collector = backgroundScope.launch { viewModel.notes.collect {} }

        viewModel.addNote("Architecture Note", "MVVM clean architecture in Kotlin", "mod_1", "/local/path/img.jpg")
        testScheduler.advanceTimeBy(500L)

        assertTrue(repository.addNoteCalled)
        assertEquals(1, viewModel.notes.value.size)
        assertEquals("Architecture Note", viewModel.notes.value.first().title)
        assertEquals("/local/path/img.jpg", viewModel.notes.value.first().attachmentUrl)
        collector.cancel()
    }

    @Test
    fun `addNote with blank title is ignored`() = runTest {
        viewModel.addNote(" ", "Body content", "mod_1")
        assertFalse(repository.addNoteCalled)
    }

    @Test
    fun `onSearchQueryChange updates query state`() = runTest {
        viewModel.onSearchQueryChange("RoomDB")
        assertEquals("RoomDB", viewModel.searchQuery.value)
    }
}
