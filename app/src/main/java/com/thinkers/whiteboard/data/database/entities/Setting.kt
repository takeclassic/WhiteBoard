package com.thinkers.whiteboard.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Setting (
    @PrimaryKey
    @ColumnInfo(name = "setting_id")
    val settingId: String,
    @ColumnInfo(name = "setting_value")
    val settingValue: String
)
