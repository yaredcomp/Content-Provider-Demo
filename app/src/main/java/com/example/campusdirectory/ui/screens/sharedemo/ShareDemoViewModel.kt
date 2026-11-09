/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * This screen demonstrates the ContentResolver acting as a CLIENT of our own provider —
 * exactly the same pattern used to read system Contacts.
 *
 * ShareDemoViewModel uses the system ContentResolver to interact with CampusContract.Contacts.CONTENT_URI.
 * It demonstrates how ANY external Android application (or this app acting as a client)
 * queries, inserts, and deletes records using standard ContentProvider protocols.
 */
package com.example.campusdirectory.ui.screens.sharedemo

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campusdirectory.provider.CampusContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CursorRowItem(
    val id: Long,
    val name: String,
    val phone: String,
    val email: String,
    val course: String,
    val rawSummary: String
)

data class ShareDemoUiState(
    val cursorRows: List<CursorRowItem> = emptyList(),
    val totalRows: Int = 0,
    val isLoading: Boolean = false,
    val lastAction: String = "No query executed yet",
    val error: String? = null
)

sealed interface ShareDemoEvent {
    data class ShowSnackbar(val message: String) : ShareDemoEvent
}

class ShareDemoViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val contentResolver: ContentResolver = application.contentResolver

    private val _uiState = MutableStateFlow(ShareDemoUiState())
    val uiState: StateFlow<ShareDemoUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ShareDemoEvent>()
    val events: SharedFlow<ShareDemoEvent> = _events.asSharedFlow()

    init {
        // Automatically perform initial query on entry so students immediately see data
        queryContentProvider()
    }

    /**
     * Executes contentResolver.query() against our own custom content:// URI.
     */
    fun queryContentProvider() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val results = withContext(Dispatchers.IO) {
                    val rows = mutableListOf<CursorRowItem>()
                    val projection = arrayOf(
                        CampusContract.Contacts.ID,
                        CampusContract.Contacts.NAME,
                        CampusContract.Contacts.PHONE,
                        CampusContract.Contacts.EMAIL,
                        CampusContract.Contacts.COURSE
                    )

                    val cursor: Cursor? = contentResolver.query(
                        CampusContract.Contacts.CONTENT_URI,
                        projection,
                        null,
                        null,
                        "${CampusContract.Contacts.NAME} ASC"
                    )

                    cursor?.use { c ->
                        val idIndex = c.getColumnIndex(CampusContract.Contacts.ID)
                        val nameIndex = c.getColumnIndex(CampusContract.Contacts.NAME)
                        val phoneIndex = c.getColumnIndex(CampusContract.Contacts.PHONE)
                        val emailIndex = c.getColumnIndex(CampusContract.Contacts.EMAIL)
                        val courseIndex = c.getColumnIndex(CampusContract.Contacts.COURSE)

                        while (c.moveToNext()) {
                            val id = if (idIndex != -1) c.getLong(idIndex) else -1L
                            val name = if (nameIndex != -1) c.getString(nameIndex) ?: "" else ""
                            val phone = if (phoneIndex != -1) c.getString(phoneIndex) ?: "" else ""
                            val email = if (emailIndex != -1) c.getString(emailIndex) ?: "" else ""
                            val course = if (courseIndex != -1) c.getString(courseIndex) ?: "" else ""

                            rows.add(
                                CursorRowItem(
                                    id = id,
                                    name = name,
                                    phone = phone,
                                    email = email,
                                    course = course,
                                    rawSummary = "Row #$id: [name=\"$name\", phone=\"$phone\", email=\"$email\", course=\"$course\"]"
                                )
                            )
                        }
                    }
                    rows
                }

                _uiState.value = _uiState.value.copy(
                    cursorRows = results,
                    totalRows = results.size,
                    isLoading = false,
                    lastAction = "Queried ${results.size} rows via ContentResolver",
                    error = null
                )
            } catch (se: SecurityException) {
                val msg = "SecurityException: Insufficient permission to query provider (${se.localizedMessage})"
                _uiState.value = _uiState.value.copy(isLoading = false, error = msg)
                _events.emit(ShareDemoEvent.ShowSnackbar(msg))
            } catch (e: Exception) {
                val msg = "Error querying ContentResolver: ${e.localizedMessage}"
                _uiState.value = _uiState.value.copy(isLoading = false, error = msg)
                _events.emit(ShareDemoEvent.ShowSnackbar(msg))
            }
        }
    }

    /**
     * Executes contentResolver.insert() on CampusContract.Contacts.CONTENT_URI.
     */
    fun insertSampleViaContentResolver() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                withContext(Dispatchers.IO) {
                    val values = ContentValues().apply {
                        put(CampusContract.Contacts.NAME, "Abebe Bikila")
                        put(CampusContract.Contacts.PHONE, "555-0911")
                        put(CampusContract.Contacts.EMAIL, "abebe@campus.edu")
                        put(CampusContract.Contacts.COURSE, "CS-301 ContentProviders")
                    }
                    val newUri = contentResolver.insert(CampusContract.Contacts.CONTENT_URI, values)
                    newUri
                }

                _events.emit(ShareDemoEvent.ShowSnackbar("Inserted 'Abebe Bikila' via ContentResolver!"))
                queryContentProvider()
            } catch (se: SecurityException) {
                val msg = "SecurityException: Write permission denied (${se.localizedMessage})"
                _uiState.value = _uiState.value.copy(isLoading = false, error = msg)
                _events.emit(ShareDemoEvent.ShowSnackbar(msg))
            } catch (e: Exception) {
                val msg = "Insert failed: ${e.localizedMessage}"
                _uiState.value = _uiState.value.copy(isLoading = false, error = msg)
                _events.emit(ShareDemoEvent.ShowSnackbar(msg))
            }
        }
    }

    /**
     * Executes contentResolver.delete() on CampusContract.Contacts.CONTENT_URI.
     */
    fun deleteSampleViaContentResolver() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val deletedCount = withContext(Dispatchers.IO) {
                    contentResolver.delete(
                        CampusContract.Contacts.CONTENT_URI,
                        "${CampusContract.Contacts.NAME} LIKE ?",
                        arrayOf("%Abebe%")
                    )
                }

                if (deletedCount > 0) {
                    _events.emit(ShareDemoEvent.ShowSnackbar("Deleted $deletedCount sample row(s) via ContentResolver!"))
                } else {
                    _events.emit(ShareDemoEvent.ShowSnackbar("No sample rows named 'Abebe' found to delete."))
                }
                queryContentProvider()
            } catch (se: SecurityException) {
                val msg = "SecurityException: Delete permission denied (${se.localizedMessage})"
                _uiState.value = _uiState.value.copy(isLoading = false, error = msg)
                _events.emit(ShareDemoEvent.ShowSnackbar(msg))
            } catch (e: Exception) {
                val msg = "Delete failed: ${e.localizedMessage}"
                _uiState.value = _uiState.value.copy(isLoading = false, error = msg)
                _events.emit(ShareDemoEvent.ShowSnackbar(msg))
            }
        }
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShareDemoViewModel::class.java)) {
                return ShareDemoViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
