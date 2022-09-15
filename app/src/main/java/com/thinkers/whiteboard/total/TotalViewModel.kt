package com.thinkers.whiteboard.total

import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.thinkers.whiteboard.database.repositories.MemoRepository

class TotalViewModel(private val memoRepository: MemoRepository) : ViewModel() {
   val allMemos = memoRepository.allMemos.asLiveData()

    val pagingMemos = memoRepository.getAllPagingMemos().cachedIn(viewModelScope).asLiveData()
}

class TotalViewModelFactory(
    private val memoRepository: MemoRepository
)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TotalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TotalViewModel(memoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
