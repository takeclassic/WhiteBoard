package com.thinkers.whiteboard.domain

import com.thinkers.whiteboard.data.database.entities.Setting
import kotlinx.coroutines.flow.Flow

interface SettingRepository {
    fun setSetting(setting: Setting)
    suspend fun getSetting(settingId: String): Flow<Setting>
}
