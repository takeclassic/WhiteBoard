package com.thinkers.whiteboard.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.thinkers.whiteboard.data.database.daos.MemoDao
import com.thinkers.whiteboard.data.database.daos.NoteDao
import com.thinkers.whiteboard.data.database.daos.SettingDao
import com.thinkers.whiteboard.data.database.entities.Memo
import com.thinkers.whiteboard.data.database.entities.MemoFTS
import com.thinkers.whiteboard.data.database.entities.Note
import com.thinkers.whiteboard.data.database.entities.Setting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Memo::class, Note::class, MemoFTS::class, Setting::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
    abstract fun noteDao(): NoteDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance() = INSTANCE

        fun getDatabase(
            context: Context,
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "whiteboard_db"
                )
                    .createFromAsset("pre-data.db")
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.execSQL("INSERT INTO memo_fts(memo_fts) VALUES ('rebuild')")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
