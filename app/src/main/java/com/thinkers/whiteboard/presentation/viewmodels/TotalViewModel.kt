package com.thinkers.whiteboard.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.thinkers.whiteboard.data.enums.MemoUpdateState
import com.thinkers.whiteboard.data.database.entities.Memo
import com.thinkers.whiteboard.domain.MemoRepository
import com.thinkers.whiteboard.presentation.fragments.TotalFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class TotalViewModel @Inject constructor(
    private val memoRepository: MemoRepository,
) : ViewModel() {
    companion object {
        const val TAG = "TotalViewModel"
    }

    val allMemos = memoRepository.getAllMemos().asLiveData()

    private var _memoList = mutableListOf<Memo>()
    val memoList: List<Memo> = _memoList
    private val _memoListLiveData = MutableLiveData<List<Memo>>()
    val memoListLiveData: LiveData<List<Memo>> = _memoListLiveData
    var memoState: MemoUpdateState = MemoUpdateState.NONE
    val mutex = Mutex()

    val memoMap = mutableMapOf<Int, Int>()
    var memoToUpdate: Memo = Memo(-1, "", 0,0, 0,"")

    fun init () {
        Log.i(TAG, "viewmodel, state: ${memoRepository.memoState}")
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
                MemoUpdateState.NONE -> {}
            }
        }
    }

    fun getMemoById(memoId: Int): LiveData<Memo> {
        return memoRepository.getMemoById(memoId).asLiveData()
    }

    fun totalMemoCount(scope: CoroutineScope): StateFlow<Int> {
        return memoRepository.getTotalMemoCount().stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = 0
        )
    }

    fun getNextPage(pageNumber: Int) {
        viewModelScope.launch {
            memoState = MemoUpdateState.NONE

            val list = memoRepository
                .getPaginatedMemoList(pageNumber + 1, TotalFragment.PAGE_SIZE)
                .filter { !memoMap.containsKey(it.memoId) }
                .filter { it.noteName != "waste_bin" }

            Log.i(TAG, "retrieved list: $list")
            mutex.withLock {
                _memoList.addAll(list)
                _memoList.sortByDescending { it.memoId }
                _memoList.withIndex().forEach { memoMap[it.value.memoId] = it.index }
                if (pageNumber == 0) {
                    memoState = MemoUpdateState.INSERT
                }
                _memoListLiveData.value = memoList
            }
        }
    }

    fun removeItems(memoListToDelete: List<Memo>) {
        viewModelScope.launch {
            mutex.withLock {
                for (memo in memoListToDelete) {
                    Log.i(TAG, "delete memo: $memo")
                    //memoRepository.deleteMemo(memo)
                    memoRepository.removeMemoToBin(memo)
                    _memoList.removeIf { it.memoId == memo.memoId }
                    memoMap.remove(memo.memoId)
                }
                _memoList.sortByDescending { it.memoId }
                _memoList.withIndex().forEach { memoMap[it.value.memoId] = it.index }
                _memoListLiveData.value = memoList
            }
        }
    }
}
