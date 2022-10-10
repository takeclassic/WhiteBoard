package com.thinkers.whiteboard.total

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.thinkers.whiteboard.common.interfaces.ActionModeDataHelper
import com.thinkers.whiteboard.common.interfaces.PagingMemoUpdateListener
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.repositories.MemoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TotalViewModel(
    private val memoRepository: MemoRepository,
    private val memoListUpdateListener: PagingMemoUpdateListener
) : ViewModel(), ActionModeDataHelper {
    val allMemos = memoRepository.allMemos.asLiveData()

    val pagingMemos = memoRepository.getAllPagingMemos().cachedIn(viewModelScope).asLiveData().distinctUntilChanged()

    val hasDataUpdated: StateFlow<Memo> = memoRepository.newMemoState

    private var _memoList = mutableListOf<Memo>()
    val memoList: List<Memo> = _memoList

    val memoMap = mutableMapOf<Int, Int>()
    var memoToUpdate: Memo = Memo(-1, "", 0,0, "")

    fun invalidateData() {
        try {
            memoRepository.dataSourceHolder.getDataSource().invalidate()
        } catch (e: NullPointerException) {
            Log.w(TAG, "${e.stackTrace}")
        }
    }

    fun initKeepUpdated() {
        viewModelScope.launch(Dispatchers.IO) {
            memoRepository.newMemoState.collectLatest { updatedMemo ->
                Log.i(TAG, "TotalFragment updatedMemo: ${updatedMemo.memoId}")
                memoToUpdate = updatedMemo
            }
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

    fun getNextPage(pageNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = memoRepository
                .getPaginatedMemoList(pageNumber + 1, TotalFragment.PAGE_SIZE)
                .filter { !memoMap.containsKey(it.memoId) }

            _memoList.addAll(list)
            _memoList.sortByDescending { it.memoId }
            _memoList.withIndex().forEach { memoMap[it.value.memoId] = it.index }
            if (memoMap.containsKey(memoToUpdate.memoId)) {
                _memoList[memoMap[memoToUpdate.memoId]!!] = memoToUpdate
            }
            memoListUpdateListener.onMemoListUpdated(memoList)
        }
    }

    override fun removeItems(memoListToDelete: List<Memo>) {
        viewModelScope.launch(Dispatchers.IO) {
            for (memo in memoListToDelete) {
                memoRepository.deleteMemo(memo)
                _memoList.removeIf { it.memoId == memo.memoId }
            }
            memoListUpdateListener.onMemoListUpdated(memoList)
        }
    }

    override fun moveItems(memoList: List<Memo>) {
        viewModelScope.launch(Dispatchers.IO) {

        }
    }

    companion object {
        val TAG = "TotalViewModel"
    }
}

class TotalViewModelFactory(
    private val memoRepository: MemoRepository,
    private val memoListUpdateListener: PagingMemoUpdateListener
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
