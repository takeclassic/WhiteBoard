package com.thinkers.whiteboard.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.thinkers.whiteboard.database.daos.MemoDao
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.entities.Note

@Database(entities = [Memo::class, Note::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "whiteboard_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}