package com.thinkers.whiteboard.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.thinkers.whiteboard.data.database.entities.Memo
import com.thinkers.whiteboard.data.database.entities.Note
import com.thinkers.whiteboard.domain.MemoRepository
import com.thinkers.whiteboard.domain.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val memoRepository: MemoRepository
    ): ViewModel() {
    companion object {
        const val TAG = "EditNoteViewModel"
    }

    val allEditableNotes: LiveData<List<Note>> = noteRepository
        .getAllNotes()
        .map{ list ->
            list.filter {
                it.noteName != "favorites"
            }.filter {
                it.noteName != "my_memo"
            }.filter {
                it.noteName != "waste_bin"
            }
        }.asLiveData()

    val allMoveableNotes: LiveData<List<Note>> = noteRepository
        .getAllNotes()
        .map{ list ->
            list.filter {
                it.noteName != "waste_bin"
            }
        }.asLiveData()

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }

    fun setDeletion(delete: Boolean) {
        noteRepository.isDeletion = delete
    }

    fun moveMemos(noteName: String, memoList: List<Memo>) {
        viewModelScope.launch {
            for (memo in memoList) {
                Log.i(TAG, "memo to move: ${memo.text}, before: ${memo.noteName}, after: $noteName")
                memo.noteName = noteName
                memoRepository.updateMemo(memo)
            }
        }
    }
}
