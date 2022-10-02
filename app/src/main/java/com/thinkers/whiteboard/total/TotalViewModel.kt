package com.thinkers.whiteboard.total

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.thinkers.whiteboard.common.interfaces.TotalPagingMemoListener
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.repositories.MemoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TotalViewModel(
    private val memoRepository: MemoRepository,
    private val memoListUpdateListener: TotalPagingMemoListener
) : ViewModel() {
    init {
        viewModelScope.launch {
            memoRepository
                .getPaginatedMemos(1, TotalFragment.PAGE_SIZE)
                .flowOn(Dispatchers.IO)
                .collectLatest {
                    val newList = it.filter { !memoMap.containsKey(it.memoId) }
                    _memoList.addAll(newList)
                    Log.i("TotalFragment", "viewmodel data init: $memoList")
                    memoListUpdateListener.onMemoListUpdated(memoList)
                    newList.forEach { memoMap.put(it.memoId, true) }
            }
        }
    }

    val allMemos = memoRepository.allMemos.asLiveData()

    val pagingMemos = memoRepository.getAllPagingMemos().cachedIn(viewModelScope).asLiveData().distinctUntilChanged()

    val hasDataUpdated: StateFlow<Boolean> = memoRepository.newMemoState

    private var _memoList = mutableListOf<Memo>()
    val memoList: List<Memo> = _memoList
    val memoMap = mutableMapOf<Int, Boolean>()

    fun invalidateData() {
        try {
            memoRepository.dataSourceHolder.getDataSource().invalidate()
        } catch (e: NullPointerException) {
            Log.w(TAG, "${e.stackTrace}")
        }
    }

    fun getMemoById(memoId: Int): LiveData<Memo> {
        return memoRepository.getMemoById(memoId).asLiveData()
    }

    fun totalMemoCount(scope: CoroutineScope): StateFlow<Int> {
        return memoRepository.totalMemoCount.stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = 0
        )
    }

    fun setPageNumber(pageNumber: Int) {
        viewModelScope.launch {
            memoRepository
                .getPaginatedMemos(pageNumber+1, TotalFragment.PAGE_SIZE)
                .flowOn(Dispatchers.IO)
                .collectLatest {
                    val newList = it.filter { !memoMap.containsKey(it.memoId) }
                    _memoList.addAll(newList)
                    Log.i("TotalFragment", "pagenumber: $pageNumber, viewmodel data changed: $it")
                    memoListUpdateListener.onMemoListUpdated(memoList)
                    newList.forEach { memoMap.put(it.memoId, true) }
            }
        }
    }

    companion object {
        val TAG = "TotalViewModel"
    }
}

class TotalViewModelFactory(
    private val memoRepository: MemoRepository,
    private val memoListUpdateListener: TotalPagingMemoListener
)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TotalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TotalViewModel(memoRepository, memoListUpdateListener) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
