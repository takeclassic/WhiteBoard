package com.thinkers.whiteboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.thinkers.whiteboard.database.entities.Note
import com.thinkers.whiteboard.database.repositories.MemoRepository
import com.thinkers.whiteboard.database.repositories.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

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
