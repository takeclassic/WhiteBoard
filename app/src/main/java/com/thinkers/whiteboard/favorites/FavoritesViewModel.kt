package com.thinkers.whiteboard.favorites

import androidx.lifecycle.*
import com.thinkers.whiteboard.database.entities.NoteAndMemos
import com.thinkers.whiteboard.database.repositories.NoteRepository

class FavoritesViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {
    val allFavorites: LiveData<NoteAndMemos?> = noteRepository.getNoteWithMemos("favorites").asLiveData()
}

class FavoritesViewModelFactory(
    private val noteRepository: NoteRepository)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(noteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")    }
}
