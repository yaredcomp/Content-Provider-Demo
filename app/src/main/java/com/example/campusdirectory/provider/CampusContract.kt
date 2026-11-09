/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * This is a CONTENT PROVIDER Contract — defines public URIs, column names,
 * and MIME types exposed to other apps and ContentResolvers.
 */
package com.example.campusdirectory.provider

import android.net.Uri

object CampusContract {
    /**
     * Unique authority string registered in AndroidManifest.xml.
     */
    const val AUTHORITY = "com.example.campusdirectory"

    /**
     * Root content URI for this ContentProvider.
     */
    val BASE_URI: Uri = Uri.parse("content://$AUTHORITY")

    /**
     * Contract definition for the "contacts" directory and individual items.
     */
    object Contacts {
        /**
         * The standard content:// URI for accessing contacts.
         * Directory: content://com.example.campusdirectory/contacts
         * Item:      content://com.example.campusdirectory/contacts/1
         */
        val CONTENT_URI: Uri = Uri.withAppendedPath(BASE_URI, "contacts")

        // Standard column names exposed by the provider
        const val ID = "_id"
        const val NAME = "name"
        const val PHONE = "phone"
        const val EMAIL = "email"
        const val COURSE = "course"

        // MIME types for directories and single items
        const val DIR_TYPE = "vnd.android.cursor.dir/vnd.com.example.campusdirectory.contacts"
        const val ITEM_TYPE = "vnd.android.cursor.item/vnd.com.example.campusdirectory.contacts"

        val DEFAULT_PROJECTION = arrayOf(ID, NAME, PHONE, EMAIL, COURSE)
    }
}
