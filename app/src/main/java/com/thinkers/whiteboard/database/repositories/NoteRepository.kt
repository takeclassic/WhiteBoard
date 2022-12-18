package com.thinkers.whiteboard.database.repositories

import androidx.annotation.WorkerThread
import androidx.paging.*
import com.thinkers.whiteboard.common.DispatcherProvider
import com.thinkers.whiteboard.database.daos.NoteDao
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.entities.Note
import com.thinkers.whiteboard.database.entities.NoteAndMemos
import com.thinkers.whiteboard.database.pagingsource.NoteAndMemosDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NoteRepository(
    private val noteDao: NoteDao,
    private val dispatchers: DispatcherProvider
    ) {
    var customNoteName: String = ""

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
