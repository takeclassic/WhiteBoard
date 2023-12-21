package com.thinkers.whiteboard.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thinkers.whiteboard.data.enums.NoteUpdateState
import com.thinkers.whiteboard.data.database.entities.Note
import com.thinkers.whiteboard.data.database.repositories.NoteRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class NewNoteViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    private val _saveNoteState = MutableSharedFlow<NoteUpdateState>()
    val saveNoteState: SharedFlow<NoteUpdateState> = _saveNoteState

    fun saveNote(note: Note) {
        viewModelScope.launch {
            val res = noteRepository.saveNote(note)
            Log.i(TAG, "res: $res")
            if (res == -1L) {
                Log.i(TAG, "call fail")
                _saveNoteState.emit(NoteUpdateState.SAVE_FAIL)
            } else {
                _saveNoteState.emit(NoteUpdateState.SAVE_SUCCESS)
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            val res = noteRepository.updateNote(note)
            if (res == 0) {
                _saveNoteState.emit(NoteUpdateState.UPDATE_FAIL)
            } else {
                noteRepository.customNoteName = note.noteName
                _saveNoteState.emit(NoteUpdateState.UPDATE_SUCCESS)
            }
        }
    }

    fun setChangedNoteNumber(num: Int) {
        noteRepository.changedNoteNumber = num
    }

    companion object {
        const val TAG = "NewNoteViewModel"
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
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}