/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * This is a QUERY PROVIDER — internal only. No URIs. No permissions. Used by the app's own UI.
 *
 * NotesViewModel manages the state of private student course notes.
 * Crucial teaching concept: Course notes remain encapsulated within the app's Room database.
 * No ContentProvider is registered for this entity, guaranteeing privacy and zero external IPC exposure.
 */
package com.example.campusdirectory.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campusdirectory.data.entity.NoteEntity
import com.example.campusdirectory.data.repository.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface NotesUiState {
    object Loading : NotesUiState
    data class Success(val notes: List<NoteEntity>) : NotesUiState
    data class Error(val message: String) : NotesUiState
}

class NotesViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    val uiState: StateFlow<NotesUiState> = noteRepository.getAllNotes()
        .map<List<NoteEntity>, NotesUiState> { notes ->
            NotesUiState.Success(notes)
        }
        .catch { e ->
            emit(NotesUiState.Error(e.message ?: "Failed to load notes"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotesUiState.Loading
        )

    class Factory(
        private val noteRepository: NoteRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
                return NotesViewModel(noteRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
