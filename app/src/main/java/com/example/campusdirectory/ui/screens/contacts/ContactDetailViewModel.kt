/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * This is a QUERY PROVIDER — internal only. No URIs. No permissions. Used by the app's own UI.
 *
 * ContactDetailViewModel loads and manages the detail state for a single classmate contact.
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

sealed interface ContactDetailUiState {
    object Loading : ContactDetailUiState
    data class Success(val contact: ContactEntity) : ContactDetailUiState
    data class Error(val message: String) : ContactDetailUiState
}

sealed interface ContactDetailEvent {
    object DeleteSuccess : ContactDetailEvent
    data class ShowMessage(val message: String) : ContactDetailEvent
}

class ContactDetailViewModel(
    private val contactId: Long,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactDetailUiState>(ContactDetailUiState.Loading)
    val uiState: StateFlow<ContactDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ContactDetailEvent>()
    val events: SharedFlow<ContactDetailEvent> = _events.asSharedFlow()

    init {
        loadContact()
    }

    fun loadContact() {
        viewModelScope.launch {
            _uiState.value = ContactDetailUiState.Loading
            try {
                val contact = contactRepository.getContactById(contactId)
                if (contact != null) {
                    _uiState.value = ContactDetailUiState.Success(contact)
                } else {
                    _uiState.value = ContactDetailUiState.Error("Contact not found")
                }
            } catch (e: Exception) {
                _uiState.value = ContactDetailUiState.Error(e.message ?: "Failed to load contact")
            }
        }
    }

    fun deleteContact() {
        viewModelScope.launch {
            try {
                contactRepository.deleteContactById(contactId)
                _events.emit(ContactDetailEvent.DeleteSuccess)
            } catch (e: Exception) {
                _events.emit(ContactDetailEvent.ShowMessage(e.message ?: "Failed to delete contact"))
            }
        }
    }

    class Factory(
        private val contactId: Long,
        private val contactRepository: ContactRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContactDetailViewModel::class.java)) {
                return ContactDetailViewModel(contactId, contactRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
