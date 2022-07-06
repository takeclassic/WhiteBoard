package com.thinkers.whiteboard.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class NoteAndMemos(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "note_name",
        entityColumn = "note_name",
    )
    val memos: List<Memo>?
)