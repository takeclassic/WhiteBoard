package com.thinkers.whiteboard.data.database.repositories

import androidx.annotation.WorkerThread
import com.thinkers.whiteboard.data.utils.DispatcherProvider
import com.thinkers.whiteboard.data.database.daos.NoteDao
import com.thinkers.whiteboard.data.database.entities.Note
import com.thinkers.whiteboard.data.database.entities.NoteAndMemos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NoteRepository(
    private val noteDao: NoteDao,
    private val dispatchers: DispatcherProvider
    ) {
    var customNoteName: String = ""
    var isDeletion: Boolean = false
    var changedNoteNumber: Int = -1

    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    fun getNote(noteName: String): Flow<Note> {
        return noteDao.getNote(noteName)
    }

    @WorkerThread
    fun getNoteWithMemos(noteName: String): Flow<NoteAndMemos?> {
        return noteDao.getNoteWithMemos(noteName)
    }

    @WorkerThread
    suspend fun saveNote(note: Note): Long = withContext(dispatchers.io) {
        noteDao.insertNote(note)
    }

    @WorkerThread
    suspend fun updateNote(note: Note): Int = withContext(dispatchers.io) {
        noteDao.updateNote(note)
    }

    @WorkerThread
    suspend fun deleteNote(note: Note) = withContext(dispatchers.io) {
        noteDao.deleteNote(note)
    }
}
