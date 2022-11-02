package com.thinkers.whiteboard.customs

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thinkers.whiteboard.common.enums.MemoUpdateState
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.repositories.MemoRepository
import com.thinkers.whiteboard.total.TotalViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CustomNoteViewModel(private val memoRepository: MemoRepository) : ViewModel() {
    private var _memoList = mutableListOf<Memo>()
    val memoList: List<Memo> = _memoList
    private val _memoListLiveData = MutableLiveData<List<Memo>>()
    val memoListLiveData: LiveData<List<Memo>> = _memoListLiveData
    val mutex = Mutex()

    var noteName: String = ""

    val memoMap = mutableMapOf<Int, Int>()
    var memoToUpdate: Memo = Memo(-1, "", 0,0, 0,"")

    fun init() {
        Log.i(TAG, "state: ${memoRepository.memoState}")
        if (memoRepository.memoState == MemoUpdateState.NONE) {
            return
        }
        memoToUpdate = memoRepository.updatedMemo
        viewModelScope.launch {
            when(memoRepository.memoState) {
                MemoUpdateState.INSERT -> {
                    getNextPage(0, noteName)
                }
                MemoUpdateState.UPDATE -> {
                    if (memoMap.containsKey(memoToUpdate.memoId)) {
                        _memoList[memoMap[memoToUpdate.memoId]!!] = memoToUpdate
                        _memoListLiveData.value = memoList
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
                MemoUpdateState.NONE -> {}
            }
        }
    }

    fun allPagingCustomNotes(noteName: String): LiveData<PagingData<Memo>> {
        return memoRepository.getCustomPagingMemos(noteName).cachedIn(viewModelScope).asLiveData()
    }

    fun customNoteMemoCount(scope: CoroutineScope, noteName: String): StateFlow<Int> {
        return memoRepository.getCustomNoteMemoCount(noteName).stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = 0
        )
    }

    fun getNextPage(pageNumber: Int, noteName: String) {
        viewModelScope.launch {
            val list = memoRepository
                .getPaginatedMemosByNoteName(noteName, pageNumber + 1, CustomNoteFragment.PAGE_SIZE)
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
        const val TAG = "CustomNoteViewModel"
    }
}

class CustomNoteViewModelFactory(
    private val memoRepository: MemoRepository
)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomNoteViewModel(memoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
