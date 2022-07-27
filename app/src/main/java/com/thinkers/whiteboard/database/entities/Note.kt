package com.thinkers.whiteboard.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey @ColumnInfo(name = "note_name") val noteName: String,
    @ColumnInfo(name = "created_time") val createdTime: Long,
    @ColumnInfo(name = "note_color") val noteColor: Int
)
