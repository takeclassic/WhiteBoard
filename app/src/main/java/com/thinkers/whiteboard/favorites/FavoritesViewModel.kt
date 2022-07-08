package com.thinkers.whiteboard.favorites

import androidx.lifecycle.*
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.entities.NoteAndMemos
import com.thinkers.whiteboard.database.repositories.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FavoritesViewModel(noteRepository: NoteRepository) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is gallery Fragment"
    }
    val text: LiveData<String> = _text

    val allFavorites: LiveData<NoteAndMemos?> = noteRepository.getNoteWithMemos("favorites").asLiveData()
}

class FavoritesViewModelFactory(private val noteRepository: NoteRepository)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(noteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")    }
}