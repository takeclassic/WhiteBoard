package com.thinkers.whiteboard.data.database.repositories

import androidx.annotation.WorkerThread
import com.thinkers.whiteboard.data.utils.DispatcherProvider
import com.thinkers.whiteboard.data.database.daos.SettingDao
import com.thinkers.whiteboard.data.database.entities.Setting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingRepository(
    private val settingDao: SettingDao,
    private val dispatchers: DispatcherProvider
) {
    @WorkerThread
    fun setSetting(setting: Setting) {
        CoroutineScope(dispatchers.io).launch {
            settingDao.saveSetting(setting)
        }
    }

    @WorkerThread
    suspend fun getSetting(settingId: String) = withContext(dispatchers.io) {
        settingDao.getSettingValueById(settingId)
    }
}
