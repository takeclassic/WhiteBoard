package com.thinkers.whiteboard.customs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.thinkers.whiteboard.database.entities.Note
import com.thinkers.whiteboard.database.repositories.NoteRepository

class NewNoteViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    suspend fun saveNote(note: Note): Long {
        return noteRepository.saveNote(note)
    }
}

class NewNoteViewModelFactory(
    private val noteRepository: NoteRepository
)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewNoteViewModel(noteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")    }
}