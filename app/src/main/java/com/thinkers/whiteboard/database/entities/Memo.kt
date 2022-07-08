package com.thinkers.whiteboard.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Memo(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "memo_id") val memoId: Int,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "created_time") val createdTime: Long,
    @ColumnInfo(name = "revised_time") val revisedTime: Long?,
    @ColumnInfo(name = "note_name") val noteName: String
)
