/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * DEPENDENCY INJECTION: AppContainer
 * Provides single instances of AppDatabase, ContactRepository, and NoteRepository
 * across the application lifecycle. Clean constructor injection without annotation processing.
 */
package com.example.campusdirectory.di

import android.content.Context
import com.example.campusdirectory.data.db.AppDatabase
import com.example.campusdirectory.data.repository.ContactRepository
import com.example.campusdirectory.data.repository.NoteRepository

interface AppContainer {
    val database: AppDatabase
    val contactRepository: ContactRepository
    val noteRepository: NoteRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    override val contactRepository: ContactRepository by lazy {
        ContactRepository(database.contactDao())
    }

    override val noteRepository: NoteRepository by lazy {
        NoteRepository(database.noteDao())
    }
}
