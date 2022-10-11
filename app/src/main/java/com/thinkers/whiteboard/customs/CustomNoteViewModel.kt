package com.thinkers.whiteboard.customs

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thinkers.whiteboard.common.interfaces.PagingMemoUpdateListener
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.entities.NoteAndMemos
import com.thinkers.whiteboard.database.repositories.MemoRepository
import com.thinkers.whiteboard.database.repositories.NoteRepository
import com.thinkers.whiteboard.favorites.FavoritesFragment
import com.thinkers.whiteboard.favorites.FavoritesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomNoteViewModel(
    private val memoRepository: MemoRepository,
    private val memoListUpdateListener: PagingMemoUpdateListener
) : ViewModel() {
    private var _memoList = mutableListOf<Memo>()
    val memoList: List<Memo> = _memoList

    val memoMap = mutableMapOf<Int, Int>()
    var memoToUpdate: Memo = Memo(-1, "", 0,0, 0,"")

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

    fun initKeepUpdated() {
        viewModelScope.launch(Dispatchers.IO) {
            memoRepository.newMemoState.collectLatest { updatedMemo ->
                Log.i(TAG, "updatedMemo: ${updatedMemo.memoId}")
                memoToUpdate = updatedMemo
            }
        }
    }

    fun getNextPage(pageNumber: Int, noteName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = memoRepository
                .getPaginatedMemosByNoteName(noteName, pageNumber + 1, CustomNoteFragment.PAGE_SIZE)
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

    companion object {
        const val TAG = "CustomNoteViewModel"
    }
}

class CustomNoteViewModelFactory(
    private val memoRepository: MemoRepository,
    private val memoListUpdateListener: PagingMemoUpdateListener
)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomNoteViewModel(memoRepository, memoListUpdateListener) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
