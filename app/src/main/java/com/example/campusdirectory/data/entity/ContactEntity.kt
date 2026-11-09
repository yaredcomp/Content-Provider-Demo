/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * DATA ENTITY: ContactEntity
 * Represents a saved classmate in the campus directory.
 * Stored in Room SQLite and shared by BOTH:
 *  1) Query Provider (ContactDao/ContactRepository) for internal app UI
 *  2) Content Provider (CampusContactProvider) for external URI consumers
 */
package com.example.campusdirectory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String,
    val course: String
)
