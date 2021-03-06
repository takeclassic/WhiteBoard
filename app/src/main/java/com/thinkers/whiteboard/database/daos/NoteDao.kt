package com.thinkers.whiteboard.database.daos

import androidx.room.*
import com.thinkers.whiteboard.database.entities.Note
import com.thinkers.whiteboard.database.entities.NoteAndMemos
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM note")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE note_name LIKE (:noteName)")
    fun getNote(noteName: String): Flow<Note>

    @Transaction
    @Query("SELECT * FROM note WHERE note_name = :noteName ORDER BY created_time DESC")
    fun getNoteWithMemos(noteName: String): Flow<NoteAndMemos?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNote(note: Note): Long

    @Update
    fun updateNote(note: Note)

    @Delete
    fun deleteNote(note: Note)
}