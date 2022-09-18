package com.thinkers.whiteboard.favorites

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.repositories.MemoRepository

class FavoritesViewModel(
    private val memoRepository: MemoRepository
) : ViewModel() {
    val allFavorites: LiveData<List<Memo>> = memoRepository.allFavoriteMemos.asLiveData()
    fun allPagingFavoriteNotes(): LiveData<PagingData<Memo>> {
        return memoRepository.getFavoritePagingMemos().cachedIn(viewModelScope).asLiveData()
    }
}

class FavoritesViewModelFactory(
    private val memoRepository: MemoRepository)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(memoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")    }
}
