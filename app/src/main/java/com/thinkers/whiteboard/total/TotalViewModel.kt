package com.thinkers.whiteboard.total

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.thinkers.whiteboard.common.enums.MemoUpdateState
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.repositories.MemoRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TotalViewModel(
    private val memoRepository: MemoRepository,
) : ViewModel() {
    val allMemos = memoRepository.allMemos.asLiveData()

    val pagingMemos = memoRepository.getAllPagingMemos().cachedIn(viewModelScope).asLiveData().distinctUntilChanged()

    private var _memoList = mutableListOf<Memo>()
    val memoList: List<Memo> = _memoList
    private val _memoListLiveData = MutableLiveData<List<Memo>>()
    val memoListLiveData: LiveData<List<Memo>> = _memoListLiveData
    val mutex = Mutex()

    val memoMap = mutableMapOf<Int, Int>()
    var memoToUpdate: Memo = Memo(-1, "", 0,0, 0,"")

    fun init () {
        Log.i("KKKKK", "viewmodel, state: ${memoRepository.memoState}")
        Log.i(TAG, "state: ${memoRepository.memoState}")
        if (memoRepository.memoState == MemoUpdateState.NONE) {
            return
        }
        memoToUpdate = memoRepository.updatedMemo
        viewModelScope.launch {
            when(memoRepository.memoState) {
                MemoUpdateState.INSERT -> {
                    getNextPage(0)
                }
                MemoUpdateState.UPDATE -> {
                    if (memoMap.containsKey(memoToUpdate.memoId)) {
                        mutex.withLock {
                            _memoList[memoMap[memoToUpdate.memoId]!!] = memoToUpdate
                            _memoListLiveData.value = memoList
                        }
                    }
                }
                MemoUpdateState.DELETE -> {
                    if (memoMap.containsKey(memoToUpdate.memoId)) {
                        mutex.withLock {
                            _memoList.removeAt(memoMap[memoToUpdate.memoId]!!)
                            memoMap.remove(memoToUpdate.memoId)
                            _memoList.withIndex().forEach { memoMap[it.value.memoId] = it.index }
                            _memoListLiveData.value = memoList
                        }
                    }
                }
                else -> {}
            }
        }
    }

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

    fun getNextPage(pageNumber: Int) {
        viewModelScope.launch {
            val list = memoRepository
                .getPaginatedMemoList(pageNumber + 1, TotalFragment.PAGE_SIZE)
                .filter { !memoMap.containsKey(it.memoId) }

            Log.i(TAG, "retrieved list: $list")
            mutex.withLock {
                _memoList.addAll(list)
                _memoList.sortByDescending { it.memoId }
                _memoList.withIndex().forEach { memoMap[it.value.memoId] = it.index }
                _memoListLiveData.value = memoList
            }
        }
    }

    fun removeItems(memoListToDelete: List<Memo>) {
        viewModelScope.launch {
            mutex.withLock {
                for (memo in memoListToDelete) {
                    Log.i(TAG, "delete memo: $memo")
                    memoRepository.deleteMemo(memo)
                    _memoList.removeIf { it.memoId == memo.memoId }
                    memoMap.remove(memo.memoId)
                }
                _memoList.sortByDescending { it.memoId }
                _memoList.withIndex().forEach { memoMap[it.value.memoId] = it.index }
                _memoListLiveData.value = memoList
            }
        }
    }

    companion object {
        const val TAG = "TotalViewModel"
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
