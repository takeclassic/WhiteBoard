package com.thinkers.whiteboard.customs

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
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.deleteNote(note)
        }
    }

    fun moveMemos(noteName: String, memoList: List<Memo>) {
        viewModelScope.launch(Dispatchers.IO) {
            for (memo in memoList) {
                memo.noteName = noteName
                memoRepository.updateMemo(memo)
            }
        }
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
