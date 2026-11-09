/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * This is a QUERY PROVIDER — internal only. No URIs. No permissions. Used by the app's own UI.
 *
 * ContactRepository abstracts direct database calls from the ViewModels.
 * It uses Kotlin Coroutines and Flows for clean, high-performance in-memory and
 * SQLite operations within our application process.
 */
package com.example.campusdirectory.data.repository

import com.example.campusdirectory.data.db.ContactDao
import com.example.campusdirectory.data.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(
    private val contactDao: ContactDao
) {
    /**
     * Observes all contacts ordered alphabetically by name.
     */
    fun getAllContacts(): Flow<List<ContactEntity>> = contactDao.observeAll()

    /**
     * Searches contacts matching a query string (demonstrating internal Query Provider search).
     */
    fun searchContacts(query: String): Flow<List<ContactEntity>> {
        val sanitizedQuery = "%${query.trim()}%"
        return contactDao.search(sanitizedQuery)
    }

    suspend fun getContactById(id: Long): ContactEntity? = contactDao.getById(id)

    suspend fun saveContact(contact: ContactEntity): Long {
        return if (contact.id == 0L) {
            contactDao.insert(contact)
        } else {
            contactDao.update(contact)
            contact.id
        }
    }

    suspend fun deleteContact(contact: ContactEntity) {
        contactDao.delete(contact)
    }

    suspend fun deleteContactById(id: Long) {
        contactDao.deleteById(id)
    }
}
