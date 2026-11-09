/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * This is the unified Room SQLite Database.
 * Key Teaching Insight:
 * BOTH the internal Query Provider (DAOs/Repositories) and the external
 * Content Provider (CampusContactProvider) share this EXACT same database instance.
 * Changes made via ContentResolver are immediately reflected in Room Flow observables!
 */
package com.example.campusdirectory.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.campusdirectory.data.entity.ContactEntity
import com.example.campusdirectory.data.entity.NoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ContactEntity::class, NoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        const val DATABASE_NAME = "campus_directory_hub.db"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(appContext: Context): AppDatabase {
            return Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(PrepopulateCallback(appContext))
                .build()
        }
    }

    private class PrepopulateCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Seed 3 demo contacts and 2 demo notes on first database creation
            CoroutineScope(Dispatchers.IO).launch {
                val database = getInstance(context)
                val contactDao = database.contactDao()
                val noteDao = database.noteDao()

                // 3 Initial Classmates
                contactDao.insert(
                    ContactEntity(
                        name = "Alex Rivera",
                        phone = "555-0142",
                        email = "arivera@campus.edu",
                        course = "CS-301 Mobile Dev"
                    )
                )
                contactDao.insert(
                    ContactEntity(
                        name = "Maya Patel",
                        phone = "555-0188",
                        email = "mpatel@campus.edu",
                        course = "CS-305 Databases"
                    )
                )
                contactDao.insert(
                    ContactEntity(
                        name = "Jordan Chen",
                        phone = "555-0199",
                        email = "jchen@campus.edu",
                        course = "SE-410 Software Arch"
                    )
                )

                // 2 Initial Course Notes (Internal only)
                noteDao.insert(
                    NoteEntity(
                        title = "Ch. 5: ContentProvider vs Query Provider",
                        body = "Content Providers expose data cross-process via content:// URIs with Android permissions. Query Providers (DAOs/Repositories) query data internally within the app without URI or IPC overhead.",
                        courseTag = "CS-301"
                    )
                )
                noteDao.insert(
                    NoteEntity(
                        title = "ADB Query Testing Checklist",
                        body = "Run: adb shell content query --uri content://com.example.campusdirectory/contacts. Check that column projection returns _id, name, phone, email, and course.",
                        courseTag = "CS-301 Lab"
                    )
                )
            }
        }
    }
}
