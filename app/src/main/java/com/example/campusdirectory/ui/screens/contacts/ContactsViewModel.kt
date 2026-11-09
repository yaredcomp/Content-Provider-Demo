/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * This is a QUERY PROVIDER — internal only. No URIs. No permissions. Used by the app's own UI.
 *
 * ContactsViewModel consumes reactive Flows from ContactRepository.
 * Dynamic search input filters the database directly via ContactDao.search(),
 * demonstrating how internal screens interact with Room without IPC or ContentResolvers.
 */
package com.example.campusdirectory.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campusdirectory.data.entity.ContactEntity
import com.example.campusdirectory.data.repository.ContactRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface ContactsUiState {
    object Loading : ContactsUiState
    data class Success(
        val contacts: List<ContactEntity>,
        val searchQuery: String
    ) : ContactsUiState
    data class Error(val message: String) : ContactsUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsViewModel(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val uiState: StateFlow<ContactsUiState> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                contactRepository.getAllContacts()
            } else {
                contactRepository.searchContacts(query)
            }
        }
        .map<List<ContactEntity>, ContactsUiState> { contacts ->
            ContactsUiState.Success(contacts = contacts, searchQuery = _searchQuery.value)
        }
        .catch { e ->
            emit(ContactsUiState.Error(e.message ?: "Failed to load contacts"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ContactsUiState.Loading
        )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    class Factory(
        private val contactRepository: ContactRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
                return ContactsViewModel(contactRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
