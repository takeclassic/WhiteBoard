package com.thinkers.whiteboard

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.Room
import com.thinkers.whiteboard.common.utils.DispatcherProviderUtil
import com.thinkers.whiteboard.common.notifications.NotificationHelper
import com.thinkers.whiteboard.database.AppDatabase
import com.thinkers.whiteboard.database.repositories.MemoRepository
import com.thinkers.whiteboard.database.repositories.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class WhiteBoardApplication: Application() {
    init {
        instance = this
    }

    val database by lazy { AppDatabase.getDatabase(this) }
    val memoRepository by lazy {
        MemoRepository(
            database.memoDao(),
            DispatcherProviderUtil()
        )
    }
    val noteRepository by lazy {
        NoteRepository(
            database.noteDao(),
            DispatcherProviderUtil()
        )
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }

    companion object {
        var instance: WhiteBoardApplication? = null
        fun context(): Context {
            return instance!!.applicationContext
        }
    }
}
