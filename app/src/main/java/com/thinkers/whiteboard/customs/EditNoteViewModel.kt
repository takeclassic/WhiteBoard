package com.thinkers.whiteboard.customs

import android.util.Log
import androidx.lifecycle.*
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.entities.Note
import com.thinkers.whiteboard.database.repositories.MemoRepository
import com.thinkers.whiteboard.database.repositories.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class EditNoteViewModel(
    private val noteRepository: NoteRepository,
    private val memoRepository: MemoRepository
    ): ViewModel() {
    val allEditableNotes: LiveData<List<Note>> = noteRepository
        .allNotes
        .map{ list ->
            list.filter {
                it.noteName != "favorites"
            }.filter {
                it.noteName != "내 메모"
            }
        }.asLiveData()

    val allMoveableNotes: LiveData<List<Note>> = noteRepository.allNotes.asLiveData()

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

    companion object {
        const val TAG = "EditNoteViewModel"
    }
}

class EditNoteViewModelFactory(
    private val noteRepository: NoteRepository,
    private val memoRepository: MemoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditNoteViewModel(noteRepository, memoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
