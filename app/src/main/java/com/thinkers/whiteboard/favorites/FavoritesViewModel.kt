package com.thinkers.whiteboard.favorites

import androidx.lifecycle.*
import com.thinkers.whiteboard.common.interfaces.IMemoInfoReceiver
import com.thinkers.whiteboard.database.entities.NoteAndMemos
import com.thinkers.whiteboard.database.repositories.NoteRepository

class FavoritesViewModel(
    private val noteRepository: NoteRepository,
    private val memoInfoReceiver: IMemoInfoReceiver
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is gallery Fragment"
    }
    val text: LiveData<String> = _text

    val allFavorites: LiveData<NoteAndMemos?> = noteRepository.getNoteWithMemos("favorites").asLiveData()

    fun setMemoId(id: Int) {
        memoInfoReceiver.setMemoId(id)
    }
}

class FavoritesViewModelFactory(
    private val noteRepository: NoteRepository,
    private val memoInfoReceiver: IMemoInfoReceiver)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(noteRepository, memoInfoReceiver) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")    }
}