package com.thinkers.whiteboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.thinkers.whiteboard.data.database.entities.Note
import com.thinkers.whiteboard.data.database.repositories.MemoRepository
import com.thinkers.whiteboard.data.database.repositories.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MainActivityViewModel(
    private val noteRepository: NoteRepository,
    private val memoRepository: MemoRepository
): ViewModel() {
    val getAllCustomNotes: Flow<List<Note>> =
        noteRepository.allNotes.map {
                list -> list.filter {
                    it.noteName != "favorites"
                }.filter {
                    it.noteName != "waste_bin"
                }
        }

    fun setMemoBelongNote(noteName: String) {
       memoRepository.noteName = noteName
    }

    fun setCustomNoteName(noteName: String) {
        noteRepository.customNoteName = noteName
    }

    fun getSwtichStatus(fileName: String, key: String): Boolean {
            return memoRepository.readBooleanPreference(
                fileName,
                key,
                false
            )
    }
}

class MainActivityViewModelFactory(
    private val noteRepository: NoteRepository,
    private val memoRepository: MemoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(noteRepository, memoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
