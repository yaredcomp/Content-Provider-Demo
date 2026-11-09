/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * DATA ENTITY: NoteEntity
 * Represents a student's private course note.
 * Unlike ContactEntity, NoteEntity is STRICTLY INTERNAL.
 * It is accessed solely via Query Provider (NoteDao/NoteRepository)
 * and has NO ContentProvider URI exposure.
 */
package com.example.campusdirectory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val body: String,
    val courseTag: String
)
