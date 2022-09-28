package com.thinkers.whiteboard.total

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.thinkers.whiteboard.database.repositories.MemoRepository
import kotlinx.coroutines.flow.StateFlow

class TotalViewModel(private val memoRepository: MemoRepository) : ViewModel() {
    val allMemos = memoRepository.allMemos.asLiveData()

    val pagingMemos = memoRepository.getAllPagingMemos().cachedIn(viewModelScope).asLiveData()

    fun invalidateData() {
        try {
            memoRepository.dataSourceHolder.getDataSource().invalidate()
        } catch (e: NullPointerException) {
            Log.w(TAG, "${e.stackTrace}")
        }
    }

    val hasDataUpdated: StateFlow<Boolean> = memoRepository.newMemoState

    companion object {
        val TAG = "TotalViewModel"
    }
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
