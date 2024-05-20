package com.thinkers.whiteboard.data.repositories

import androidx.annotation.WorkerThread
import com.thinkers.whiteboard.data.database.daos.NoteDao
import com.thinkers.whiteboard.data.database.entities.Note
import com.thinkers.whiteboard.data.database.entities.NoteAndMemos
import com.thinkers.whiteboard.domain.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor (
    @Inject @JvmField var noteDao: NoteDao
) : NoteRepository {
    override var isDeletion: Boolean = false
    override var customNoteName: String = ""
    override var changedNoteNumber: Int = -1

    @WorkerThread
    override fun getNote(noteName: String): Flow<Note> {
        return noteDao.getNote(noteName)
    }

    @WorkerThread
    override fun getNoteWithMemos(noteName: String): Flow<NoteAndMemos?> {
        return noteDao.getNoteWithMemos(noteName)
    }

    @WorkerThread
    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes()
    }

    @WorkerThread
    override suspend fun saveNote(note: Note): Long = withContext(Dispatchers.IO) {
        noteDao.insertNote(note)
    }

    @WorkerThread
    override suspend fun updateNote(note: Note): Int = withContext(Dispatchers.IO) {
        noteDao.updateNote(note)
    }

    @WorkerThread
    override suspend fun deleteNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(note)
    }
}
