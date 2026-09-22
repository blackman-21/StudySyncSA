package com.studysync.sa.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studysync.sa.data.local.entity.NoteEntity
import com.studysync.sa.data.repository.StudySyncRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class NoteViewModel(private val repository: StudySyncRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val notes: StateFlow<List<NoteEntity>> = _searchQuery
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                repository.getAllNotes()
            } else {
                repository.searchNotes(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun addNote(title: String, body: String, moduleId: String) {
        if (title.isBlank()) return
        val newNote = NoteEntity(
            id = UUID.randomUUID().toString(),
            moduleId = moduleId,
            title = title,
            body = body
        )
        viewModelScope.launch {
            repository.addNote(newNote)
        }
    }
}
