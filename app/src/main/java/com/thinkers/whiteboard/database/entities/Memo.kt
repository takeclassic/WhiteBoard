package com.thinkers.whiteboard.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Memo(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "memo_id") val memoId: Int,
    @ColumnInfo(name = "title") var title: String?,
    @ColumnInfo(name = "text") var text: String,
    @ColumnInfo(name = "created_time") val createdTime: Long,
    @ColumnInfo(name = "revised_time") var revisedTime: Long?,
    @ColumnInfo(name = "note_name") var noteName: String
)
