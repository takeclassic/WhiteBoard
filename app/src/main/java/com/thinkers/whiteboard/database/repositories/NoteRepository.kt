package com.thinkers.whiteboard.database.repositories

import androidx.annotation.WorkerThread
import com.thinkers.whiteboard.database.daos.NoteDao
import com.thinkers.whiteboard.database.entities.Note
import com.thinkers.whiteboard.database.entities.NoteAndMemos
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    @WorkerThread
    fun getNoteWithMemos(noteName: String): Flow<NoteAndMemos?> {
        return noteDao.getNoteWithMemos(noteName)
    }

    @WorkerThread
    suspend fun saveNote(note: Note): Long {
        return noteDao.insertNote(note)
    }

    @WorkerThread
    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    @WorkerThread
    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }
}