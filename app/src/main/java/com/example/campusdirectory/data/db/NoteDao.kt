/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * This is a QUERY PROVIDER — internal only. No URIs. No permissions. Used by the app's own UI.
 *
 * NoteDao provides local-only access to private student course notes.
 * There is NO ContentProvider for this table, emphasizing to students
 * that sensitive or internal application data should NOT be exposed
 * via ContentProvider URIs.
 */
package com.example.campusdirectory.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.campusdirectory.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE courseTag = :courseTag ORDER BY id DESC")
    fun observeByCourseTag(courseTag: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity): Int

    @Delete
    suspend fun delete(note: NoteEntity): Int

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
