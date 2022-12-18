package com.thinkers.whiteboard.customs

import android.util.Log
import android.view.View
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thinkers.whiteboard.common.enums.MemoUpdateState
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.entities.Note
import com.thinkers.whiteboard.database.repositories.MemoRepository
import com.thinkers.whiteboard.database.repositories.NoteRepository
import com.thinkers.whiteboard.total.TotalViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class CustomNoteViewModel(
    private val noteRepository: NoteRepository,
    private val memoRepository: MemoRepository
    ) : ViewModel() {
    private var _memoList = mutableListOf<Memo>()
    val memoList: List<Memo> = _memoList
    private val _memoListLiveData = MutableLiveData<List<Memo>>()
    val memoListLiveData: LiveData<List<Memo>> = _memoListLiveData
    var memoState: MemoUpdateState = MemoUpdateState.NONE
    val mutex = Mutex()

    var noteName: String = ""

    val memoMap = mutableMapOf<Int, Int>()
    var memoToUpdate: Memo = Memo(-1, "", 0,0, 0,"")

    private var _actionModeSetMemoList = mutableListOf<Memo>()
    var actionModeSetMemoList = mutableListOf<Memo>()
    var actionModeSetViewList = mutableListOf<View>()

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

    fun getNote(noteName: String): Flow<Note> {
        return noteRepository.getNote(noteName)
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
            memoState = MemoUpdateState.NONE
            val list = memoRepository
                .getPaginatedMemosByNoteName(noteName, pageNumber + 1, CustomNoteFragment.PAGE_SIZE)
                .filter { !memoMap.containsKey(it.memoId) }

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

    fun removeMovedItems() {
        viewModelScope.launch {
            mutex.withLock {
                for (memo in _actionModeSetMemoList) {
                    Log.i(TAG, "moved memo: $memo")
                    _memoList.removeIf { it.memoId == memo.memoId }
                    memoMap.remove(memo.memoId)
                }
                _memoList.removeIf { it.memoId == memoRepository.updatedMemo.memoId }
                memoMap.remove(memoRepository.updatedMemo.memoId)
                _memoList.sortByDescending { it.memoId }
                _memoList.withIndex().forEach { memoMap[it.value.memoId] = it.index }
                _actionModeSetMemoList = mutableListOf()
                _memoListLiveData.value = memoList
            }
        }
    }

    fun clearActionModeList() {
        for (item in actionModeSetMemoList) {
            _actionModeSetMemoList.add(item)
        }
        actionModeSetMemoList = mutableListOf()
        actionModeSetViewList = mutableListOf()
    }

    companion object {
        const val TAG = "CustomNoteViewModel"
    }
}

class CustomNoteViewModelFactory(
    private val noteRepository: NoteRepository,
    private val memoRepository: MemoRepository
)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomNoteViewModel(noteRepository, memoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
