package com.thinkers.whiteboard.customs

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thinkers.whiteboard.database.entities.Note
import com.thinkers.whiteboard.database.repositories.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class NewNoteViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    fun saveNote(note: Note): Long = runBlocking(Dispatchers.IO) {
        noteRepository.saveNote(note)
    }

    fun updateNote(note: Note): Int = runBlocking(Dispatchers.IO) {
        noteRepository.updateNote(note)
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