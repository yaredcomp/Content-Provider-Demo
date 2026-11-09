/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * This is a QUERY PROVIDER — internal only. No URIs. No permissions. Used by the app's own UI.
 *
 * AddEditNoteViewModel manages note creation, editing, and deletion using NoteRepository.
 */
package com.example.campusdirectory.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campusdirectory.data.entity.NoteEntity
import com.example.campusdirectory.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddEditNoteFormState(
    val noteId: Long = 0,
    val title: String = "",
    val body: String = "",
    val courseTag: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AddEditNoteEvent {
    object SaveSuccess : AddEditNoteEvent
    object DeleteSuccess : AddEditNoteEvent
    data class ShowError(val message: String) : AddEditNoteEvent
}

class AddEditNoteViewModel(
    private val noteId: Long,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(AddEditNoteFormState(noteId = noteId))
    val formState: StateFlow<AddEditNoteFormState> = _formState.asStateFlow()

    private val _events = MutableSharedFlow<AddEditNoteEvent>()
    val events: SharedFlow<AddEditNoteEvent> = _events.asSharedFlow()

    init {
        if (noteId > 0) {
            loadExistingNote(noteId)
        }
    }

    private fun loadExistingNote(id: Long) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(id)
            if (note != null) {
                _formState.value = _formState.value.copy(
                    title = note.title,
                    body = note.body,
                    courseTag = note.courseTag
                )
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _formState.value = _formState.value.copy(title = newTitle, errorMessage = null)
    }

    fun onBodyChange(newBody: String) {
        _formState.value = _formState.value.copy(body = newBody)
    }

    fun onCourseTagChange(newCourseTag: String) {
        _formState.value = _formState.value.copy(courseTag = newCourseTag)
    }

    fun saveNote() {
        val currentState = _formState.value
        if (currentState.title.isBlank()) {
            _formState.value = currentState.copy(errorMessage = "Title is required")
            return
        }

        viewModelScope.launch {
            _formState.value = currentState.copy(isSaving = true)
            try {
                val entity = NoteEntity(
                    id = currentState.noteId,
                    title = currentState.title.trim(),
                    body = currentState.body.trim(),
                    courseTag = currentState.courseTag.trim().ifEmpty { "General" }
                )
                noteRepository.saveNote(entity)
                _events.emit(AddEditNoteEvent.SaveSuccess)
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(isSaving = false)
                _events.emit(AddEditNoteEvent.ShowError(e.message ?: "Failed to save note"))
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            try {
                noteRepository.deleteNoteById(noteId)
                _events.emit(AddEditNoteEvent.DeleteSuccess)
            } catch (e: Exception) {
                _events.emit(AddEditNoteEvent.ShowError(e.message ?: "Failed to delete note"))
            }
        }
    }

    class Factory(
        private val noteId: Long,
        private val noteRepository: NoteRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddEditNoteViewModel::class.java)) {
                return AddEditNoteViewModel(noteId, noteRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
