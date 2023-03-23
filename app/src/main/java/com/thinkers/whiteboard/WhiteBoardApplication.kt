package com.thinkers.whiteboard

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.thinkers.whiteboard.common.utils.DispatcherProviderUtil
import com.thinkers.whiteboard.common.notifications.NotificationHelper
import com.thinkers.whiteboard.database.AppDatabase
import com.thinkers.whiteboard.database.repositories.MemoRepository
import com.thinkers.whiteboard.database.repositories.NoteRepository
import com.thinkers.whiteboard.settings.AutoRemoveWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.TimeUnit

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
        startAutoRemove()
    }

    private fun startAutoRemove() {
        val isAutoRemoveOn = memoRepository.readBooleanPreference(
            context().getString(R.string.file_shared_preference_auto_remove),
            context().getString(R.string.key_auto_remove),
            false
        )
        Log.i(TAG, "isAutoRemoveOn: $isAutoRemoveOn")

        if (isAutoRemoveOn) {
            val autoRemoveRequest = PeriodicWorkRequestBuilder<AutoRemoveWorker>(
                4, TimeUnit.HOURS,
                1, TimeUnit.HOURS
            ).build()

            WorkManager
                .getInstance(context())
                .enqueue(autoRemoveRequest)
        }
    }

    companion object {
        var instance: WhiteBoardApplication? = null
        fun context(): Context {
            return instance!!.applicationContext
        }

        const val TAG = "WhiteBoardApplication"
    }
}
