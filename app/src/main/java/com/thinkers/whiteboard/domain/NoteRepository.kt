package com.thinkers.whiteboard.domain

import com.thinkers.whiteboard.data.database.entities.Note
import com.thinkers.whiteboard.data.database.entities.NoteAndMemos
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    var customNoteName: String
    var changedNoteNumber: Int
    var isDeletion: Boolean

    fun getNote(noteName: String): Flow<Note>

    fun getNoteWithMemos(noteName: String): Flow<NoteAndMemos?>

    fun getAllNotes(): Flow<List<Note>>

    suspend fun saveNote(note: Note): Long

    suspend fun updateNote(note: Note): Int

    suspend fun deleteNote(note: Note)
}