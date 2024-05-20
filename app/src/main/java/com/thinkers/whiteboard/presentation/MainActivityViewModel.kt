package com.thinkers.whiteboard.presentation

import androidx.lifecycle.ViewModel
import com.thinkers.whiteboard.data.database.entities.Note
import com.thinkers.whiteboard.domain.MemoRepository
import com.thinkers.whiteboard.domain.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val memoRepository: MemoRepository
): ViewModel() {
    val getAllCustomNotes: Flow<List<Note>> =
        noteRepository.getAllNotes().map {
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
