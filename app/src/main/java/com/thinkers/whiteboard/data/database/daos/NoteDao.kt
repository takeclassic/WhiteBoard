package com.thinkers.whiteboard.data.database.daos

import androidx.room.*
import com.thinkers.whiteboard.data.database.entities.Note
import com.thinkers.whiteboard.data.database.entities.NoteAndMemos
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM note")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE note_name LIKE :noteName")
    fun getNote(noteName: String): Flow<Note>

    @Transaction
    @Query("SELECT * FROM note WHERE note_name = :noteName ORDER BY created_time DESC")
    fun getNoteWithMemos(noteName: String): Flow<NoteAndMemos?>

    @Transaction
    @Query("SELECT * FROM note WHERE note_name = :noteName ORDER BY created_time DESC")
    fun getPaginatedNoteWithMemos(noteName: String): NoteAndMemos

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNote(note: Note): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateNote(note: Note): Int

    @Delete
    fun deleteNote(note: Note)
}