package com.thinkers.whiteboard.data.database.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import org.jetbrains.annotations.NotNull

@Entity(
    indices = [
        Index(value = ["note_name"], unique = true)
    ]
)
@Parcelize
data class Note(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_number")
    val noteNumber: Int,

    @ColumnInfo(name = "note_name")
    val noteName: String,

    @ColumnInfo(name = "created_time")
    val createdTime: Long,

    @ColumnInfo(name = "note_color")
    val noteColor: Int
) : Parcelable
