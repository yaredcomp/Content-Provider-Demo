/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * This is a QUERY PROVIDER — internal only. No URIs. No permissions. Used by the app's own UI.
 *
 * AddEditContactViewModel manages form state for saving/editing contacts via ContactRepository.
 * Because the Room database is shared with CampusContactProvider, saving here makes the new
 * contact immediately visible to external ContentResolvers and ADB queries!
 */
package com.example.campusdirectory.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campusdirectory.data.entity.ContactEntity
import com.example.campusdirectory.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddEditContactFormState(
    val contactId: Long = 0,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val course: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AddEditContactEvent {
    object SaveSuccess : AddEditContactEvent
    data class ShowError(val message: String) : AddEditContactEvent
}

class AddEditContactViewModel(
    private val contactId: Long,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(AddEditContactFormState(contactId = contactId))
    val formState: StateFlow<AddEditContactFormState> = _formState.asStateFlow()

    private val _events = MutableSharedFlow<AddEditContactEvent>()
    val events: SharedFlow<AddEditContactEvent> = _events.asSharedFlow()

    init {
        if (contactId > 0) {
            loadExistingContact(contactId)
        }
    }

    private fun loadExistingContact(id: Long) {
        viewModelScope.launch {
            val contact = contactRepository.getContactById(id)
            if (contact != null) {
                _formState.value = _formState.value.copy(
                    name = contact.name,
                    phone = contact.phone,
                    email = contact.email,
                    course = contact.course
                )
            }
        }
    }

    fun onNameChange(newName: String) {
        _formState.value = _formState.value.copy(name = newName, errorMessage = null)
    }

    fun onPhoneChange(newPhone: String) {
        _formState.value = _formState.value.copy(phone = newPhone)
    }

    fun onEmailChange(newEmail: String) {
        _formState.value = _formState.value.copy(email = newEmail)
    }

    fun onCourseChange(newCourse: String) {
        _formState.value = _formState.value.copy(course = newCourse)
    }

    fun saveContact() {
        val currentState = _formState.value
        if (currentState.name.isBlank()) {
            _formState.value = currentState.copy(errorMessage = "Full name is required")
            return
        }

        viewModelScope.launch {
            _formState.value = currentState.copy(isSaving = true)
            try {
                val entity = ContactEntity(
                    id = currentState.contactId,
                    name = currentState.name.trim(),
                    phone = currentState.phone.trim(),
                    email = currentState.email.trim(),
                    course = currentState.course.trim().ifEmpty { "General" }
                )
                contactRepository.saveContact(entity)
                _events.emit(AddEditContactEvent.SaveSuccess)
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(isSaving = false)
                _events.emit(AddEditContactEvent.ShowError(e.message ?: "Failed to save contact"))
            }
        }
    }

    class Factory(
        private val contactId: Long,
        private val contactRepository: ContactRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddEditContactViewModel::class.java)) {
                return AddEditContactViewModel(contactId, contactRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
