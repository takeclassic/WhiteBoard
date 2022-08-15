package com.thinkers.whiteboard.database.entities

import androidx.room.*

@Entity(
    foreignKeys = [
        ForeignKey(
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
            entity = Note::class,
            parentColumns = ["note_name"],
            childColumns = ["note_name"]
        )
    ],
    indices = [
        Index("note_name")
    ]
)
data class Memo(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "memo_id")
    val memoId: Int,
    @ColumnInfo(name = "title")
    var title: String?,
    @ColumnInfo(name = "text")
    var text: String,
    @ColumnInfo(name = "created_time")
    val createdTime: Long,
    @ColumnInfo(name = "revised_time")
    var revisedTime: Long?,
    @ColumnInfo(name = "note_name")
    var noteName: String,
    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean = false
)
