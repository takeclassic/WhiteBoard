package com.thinkers.whiteboard.data.database.entities

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize
import org.jetbrains.annotations.NotNull

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
@Parcelize
data class Memo(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "memo_id")
    val memoId: Int,
    @ColumnInfo(name = "text")
    var text: String,
    @ColumnInfo(name = "created_time")
    val createdTime: Long,
    @ColumnInfo(name = "alarm_time")
    var alarmTime: Long?,
    @ColumnInfo(name = "revised_time")
    var revisedTime: Long?,
    @ColumnInfo(name = "note_name")
    var noteName: String,
    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean = false
): Parcelable
