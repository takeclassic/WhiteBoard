package com.thinkers.whiteboard.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thinkers.whiteboard.data.database.entities.Setting
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {
    @Query("SELECT * FROM setting WHERE setting_id LIKE (:settingId)")
    fun getSettingValueById(settingId: String): Flow<Setting>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSetting(setting: Setting)
}
