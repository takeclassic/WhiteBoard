package com.thinkers.whiteboard.data.repositories

import androidx.annotation.WorkerThread
import com.thinkers.whiteboard.data.database.daos.SettingDao
import com.thinkers.whiteboard.data.database.entities.Setting
import com.thinkers.whiteboard.domain.SettingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingRepositoryImpl @Inject constructor (
    @Inject @JvmField var settingDao: SettingDao
): SettingRepository {
    @WorkerThread
    override fun setSetting(setting: Setting) {
        CoroutineScope(Dispatchers.IO).launch {
            settingDao.saveSetting(setting)
        }
    }

    @WorkerThread
    override suspend fun getSetting(settingId: String) = withContext(Dispatchers.IO) {
        settingDao.getSettingValueById(settingId)
    }
}
