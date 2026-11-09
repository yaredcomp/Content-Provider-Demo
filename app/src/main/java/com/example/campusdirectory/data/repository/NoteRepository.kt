/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * This is a QUERY PROVIDER — internal only. No URIs. No permissions. Used by the app's own UI.
 *
 * NoteRepository encapsulates personal note operations. Demonstrates standard
 * MVVM repository pattern without any external content URI exposure.
 */
package com.example.campusdirectory.data.repository

import com.example.campusdirectory.data.db.NoteDao
import com.example.campusdirectory.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao
) {
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.observeAll()

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getById(id)

    suspend fun saveNote(note: NoteEntity): Long {
        return if (note.id == 0L) {
            noteDao.insert(note)
        } else {
            noteDao.update(note)
            note.id
        }
    }

    suspend fun deleteNote(note: NoteEntity) {
        noteDao.delete(note)
    }

    suspend fun deleteNoteById(id: Long) {
        noteDao.deleteById(id)
    }
}
