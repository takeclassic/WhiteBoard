package com.thinkers.whiteboard.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.thinkers.whiteboard.data.database.DatabaseModule.getDatabase
import com.thinkers.whiteboard.data.database.daos.MemoDao
import com.thinkers.whiteboard.data.database.daos.NoteDao
import com.thinkers.whiteboard.data.database.daos.SettingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    fun getDatabase(
        context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
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
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase = getDatabase(context)

    @Singleton
    @Provides
    fun provideMemoDao(appDatabase: AppDatabase): MemoDao = appDatabase.memoDao()

    @Singleton
    @Provides
    fun provideNoteDao(appDatabase: AppDatabase): NoteDao = appDatabase.noteDao()

    @Singleton
    @Provides
    fun provideSettingDao(appDatabase: AppDatabase): SettingDao = appDatabase.settingDao()
}
