/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * This is a CONTENT PROVIDER — exposes data via content:// URIs to OTHER apps.
 * Enforced by permissions declared in the manifest.
 *
 * It bridges external inter-process communication (IPC) requests with the underlying
 * Room SQLite database. Any third-party application with the appropriate permission
 * or ADB terminal commands can query, insert, update, and delete campus contacts.
 */
package com.example.campusdirectory.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import androidx.room.OnConflictStrategy
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.example.campusdirectory.data.db.AppDatabase

class CampusContactProvider : ContentProvider() {

    private lateinit var database: AppDatabase

    companion object {
        private const val CODE_CONTACTS_DIR = 100
        private const val CODE_CONTACT_ITEM = 101

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            // content://com.example.campusdirectory/contacts -> Directory of all contacts
            addURI(CampusContract.AUTHORITY, "contacts", CODE_CONTACTS_DIR)
            // content://com.example.campusdirectory/contacts/# -> Specific contact by ID
            addURI(CampusContract.AUTHORITY, "contacts/#", CODE_CONTACT_ITEM)
        }

        private val DEFAULT_COLUMNS = arrayOf("id AS _id", "name", "phone", "email", "course")
    }

    override fun onCreate(): Boolean {
        val ctx = context ?: return false
        // Connect to the shared AppDatabase instance
        database = AppDatabase.getInstance(ctx)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val match = uriMatcher.match(uri)
        val builder = SupportSQLiteQueryBuilder.builder("contacts")

        // Map external projection or use default
        val columnsToUse = if (projection != null && projection.isNotEmpty()) {
            projection.map { col ->
                if (col == "_id" || col == CampusContract.Contacts.ID) "id AS _id" else col
            }.toTypedArray()
        } else {
            DEFAULT_COLUMNS
        }
        builder.columns(columnsToUse)

        val normalizedSelection = selection?.replace("_id", "id")

        when (match) {
            CODE_CONTACTS_DIR -> {
                if (!normalizedSelection.isNullOrEmpty()) {
                    builder.selection(normalizedSelection, selectionArgs)
                }
            }
            CODE_CONTACT_ITEM -> {
                val contactId = ContentUris.parseId(uri)
                val itemWhere = "id = $contactId" + if (!normalizedSelection.isNullOrEmpty()) " AND ($normalizedSelection)" else ""
                builder.selection(itemWhere, selectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown or unsupported query URI: $uri")
        }

        val effectiveSortOrder = sortOrder ?: "name ASC"
        builder.orderBy(effectiveSortOrder)

        val db = database.openHelper.readableDatabase
        val cursor = db.query(builder.create())

        // Register URI for real-time notification changes across processes
        context?.let { ctx ->
            cursor.setNotificationUri(ctx.contentResolver, uri)
        }

        return cursor
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            CODE_CONTACTS_DIR -> CampusContract.Contacts.DIR_TYPE
            CODE_CONTACT_ITEM -> CampusContract.Contacts.ITEM_TYPE
            else -> throw IllegalArgumentException("Unknown or unsupported URI for MIME type: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val match = uriMatcher.match(uri)
        if (match != CODE_CONTACTS_DIR) {
            throw IllegalArgumentException("Cannot insert into URI: $uri. Use ${CampusContract.Contacts.CONTENT_URI}")
        }

        val valuesToInsert = ContentValues().apply {
            if (values != null) {
                put("name", values.getAsString(CampusContract.Contacts.NAME) ?: "Unknown Student")
                put("phone", values.getAsString(CampusContract.Contacts.PHONE) ?: "")
                put("email", values.getAsString(CampusContract.Contacts.EMAIL) ?: "")
                put("course", values.getAsString(CampusContract.Contacts.COURSE) ?: "General")
            }
        }

        val db = database.openHelper.writableDatabase
        val newRowId = db.insert("contacts", OnConflictStrategy.REPLACE, valuesToInsert)

        if (newRowId > 0) {
            val returnUri = ContentUris.withAppendedId(CampusContract.Contacts.CONTENT_URI, newRowId)
            context?.contentResolver?.notifyChange(returnUri, null)
            return returnUri
        }

        return null
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val db = database.openHelper.writableDatabase
        val rowsUpdated: Int
        val nonNullValues = values ?: ContentValues()

        // Normalize "_id" in selection to internal "id"
        val normalizedSelection = selection?.replace("_id", "id")

        when (uriMatcher.match(uri)) {
            CODE_CONTACTS_DIR -> {
                rowsUpdated = db.update("contacts", OnConflictStrategy.REPLACE, nonNullValues, normalizedSelection, selectionArgs)
            }
            CODE_CONTACT_ITEM -> {
                val id = ContentUris.parseId(uri)
                val itemWhere = "id = $id" + if (!normalizedSelection.isNullOrEmpty()) " AND ($normalizedSelection)" else ""
                rowsUpdated = db.update("contacts", OnConflictStrategy.REPLACE, nonNullValues, itemWhere, selectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI for update: $uri")
        }

        if (rowsUpdated > 0) {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return rowsUpdated
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val db = database.openHelper.writableDatabase
        val rowsDeleted: Int

        // Normalize "_id" in selection to internal "id"
        val normalizedSelection = selection?.replace("_id", "id")

        when (uriMatcher.match(uri)) {
            CODE_CONTACTS_DIR -> {
                rowsDeleted = db.delete("contacts", normalizedSelection, selectionArgs)
            }
            CODE_CONTACT_ITEM -> {
                val id = ContentUris.parseId(uri)
                val itemWhere = "id = $id" + if (!normalizedSelection.isNullOrEmpty()) " AND ($normalizedSelection)" else ""
                rowsDeleted = db.delete("contacts", itemWhere, selectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI for delete: $uri")
        }

        if (rowsDeleted > 0) {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return rowsDeleted
    }
}
