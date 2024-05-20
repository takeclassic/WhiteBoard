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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Database(entities = [Memo::class, Note::class, MemoFTS::class, Setting::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
    abstract fun noteDao(): NoteDao
    abstract fun settingDao(): SettingDao
}
