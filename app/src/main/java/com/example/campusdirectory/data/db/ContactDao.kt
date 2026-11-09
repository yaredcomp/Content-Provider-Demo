/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * This is a QUERY PROVIDER — internal only. No URIs. No permissions. Used by the app's own UI.
 *
 * ContactDao defines the internal query contract between our Room database and
 * ContactRepository. It exposes reactive Kotlin Flow streams for real-time UI updates
 * as well as direct Cursor queries utilized by CampusContactProvider.
 */
package com.example.campusdirectory.data.db

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.campusdirectory.data.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    // --- QUERY PROVIDER METHODS (Used by the internal app UI via Repository) ---

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun observeAll(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE name LIKE :q ORDER BY name ASC")
    fun search(q: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getById(id: Long): ContactEntity?

    @Query("SELECT * FROM contacts WHERE id = :id")
    fun getByIdSync(id: Long): ContactEntity?

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllSync(): List<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(c: ContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(c: ContactEntity): Long

    @Update
    suspend fun update(c: ContactEntity): Int

    @Update
    fun updateSync(c: ContactEntity): Int

    @Delete
    suspend fun delete(c: ContactEntity): Int

    @Delete
    fun deleteSync(c: ContactEntity): Int

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM contacts WHERE id = :id")
    fun deleteByIdSync(id: Long): Int

    // --- CONTENT PROVIDER BRIDGING METHODS (Returning raw Cursors) ---

    @Query("SELECT id AS _id, name, phone, email, course FROM contacts ORDER BY name ASC")
    fun queryAllCursor(): Cursor

    @Query("SELECT id AS _id, name, phone, email, course FROM contacts WHERE id = :id")
    fun queryCursorById(id: Long): Cursor

    @RawQuery
    fun queryRaw(query: SupportSQLiteQuery): Cursor
}
