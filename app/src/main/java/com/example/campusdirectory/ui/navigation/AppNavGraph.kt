/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * NAVIGATION GRAPH: AppNavGraph
 * Centralizes application destinations and back-stack navigation.
 */
package com.example.campusdirectory.ui.navigation

object AppDestinations {
    const val HOME_ROUTE = "home"
    const val CONTACT_DETAIL_ROUTE = "contact_detail/{contactId}"
    const val ADD_EDIT_CONTACT_ROUTE = "add_edit_contact?contactId={contactId}"
    const val ADD_EDIT_NOTE_ROUTE = "add_edit_note?noteId={noteId}"
    const val SHARE_DEMO_ROUTE = "share_demo"

    fun contactDetail(contactId: Long) = "contact_detail/$contactId"
    fun addContact() = "add_edit_contact"
    fun editContact(contactId: Long) = "add_edit_contact?contactId=$contactId"
    fun addNote() = "add_edit_note"
    fun editNote(noteId: Long) = "add_edit_note?noteId=$noteId"
    fun shareDemo() = SHARE_DEMO_ROUTE
}
