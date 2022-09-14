package com.thinkers.whiteboard.customs

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.entities.NoteAndMemos
import com.thinkers.whiteboard.database.repositories.NoteRepository
import com.thinkers.whiteboard.favorites.FavoritesViewModel

class CustomNoteViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {
    fun allCustomNotes(noteName: String): LiveData<NoteAndMemos?> {
        return noteRepository.getNoteWithMemos(noteName).asLiveData()
    }

    fun allPagingCustomNotes(noteName: String): LiveData<PagingData<Memo>> {
        return noteRepository.getPagingNoteAndMemos(noteName).cachedIn(viewModelScope).asLiveData()
    }
}

class CustomNoteViewModelFactory(
    private val noteRepository: NoteRepository
)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomNoteViewModel(noteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")    }
}
