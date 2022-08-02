package com.thinkers.whiteboard.database.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.versionedparcelable.VersionedParcelize
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Note(
    @PrimaryKey @ColumnInfo(name = "note_name") val noteName: String,
    @ColumnInfo(name = "created_time") val createdTime: Long,
    @ColumnInfo(name = "note_color") val noteColor: Int
) : Parcelable
