package com.thinkers.whiteboard.favorites

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thinkers.whiteboard.common.enums.MemoUpdateState
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.repositories.MemoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FavoritesViewModel(
    private val memoRepository: MemoRepository
) : ViewModel() {
    val allFavorites: LiveData<List<Memo>> = memoRepository.allFavoriteMemos.asLiveData()

    private var _memoList = mutableListOf<Memo>()
    val memoList: List<Memo> = _memoList
    private val _memoListLiveData = MutableLiveData<List<Memo>>()
    val memoListLiveData: LiveData<List<Memo>> = _memoListLiveData
    var memoState: MemoUpdateState = MemoUpdateState.NONE
    val mutex = Mutex()

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
                    getNextPage(0)
                }
                MemoUpdateState.UPDATE -> {
                    mutex.withLock {
                        if (memoMap.containsKey(memoToUpdate.memoId)) {
                            if (memoToUpdate.isFavorite) {
                                _memoList[memoMap[memoToUpdate.memoId]!!] = memoToUpdate
                            } else {
                                _memoList.removeAt(memoMap[memoToUpdate.memoId]!!)
                                memoMap.remove(memoToUpdate.memoId)
                            }
                        }
                        _memoList.withIndex().forEach { memoMap[it.value.memoId] = it.index }
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

    fun allPagingFavoriteNotes(): LiveData<PagingData<Memo>> {
        return memoRepository.getFavoritePagingMemos().cachedIn(viewModelScope).asLiveData()
    }

    fun FavoriteMemoCount(scope: CoroutineScope): StateFlow<Int> {
        return memoRepository.favoritesMemoCount.stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = 0
        )
    }

    fun getNextPage(pageNumber: Int) {
        viewModelScope.launch {
            memoState = MemoUpdateState.NONE
            val list = memoRepository
                .getPaginatedFavoriteMemoList(pageNumber + 1, FavoritesFragment.PAGE_SIZE)
                .filter { !memoMap.containsKey(it.memoId) }

            Log.i(TAG, "retrieved list: $list")

            mutex.withLock {
                _memoList.addAll(list)
                _memoList.sortByDescending { it.memoId }
                _memoList.withIndex().forEach { memoMap[it.value.memoId] = it.index }
                if (memoMap.containsKey(memoToUpdate.memoId)) {
                    if (memoToUpdate.isFavorite) {
                        _memoList[memoMap[memoToUpdate.memoId]!!] = memoToUpdate
                    } else {
                        _memoList.removeAt(memoMap[memoToUpdate.memoId]!!)
                        memoMap.remove(memoToUpdate.memoId)
                    }
                }
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

    companion object {
        const val TAG = "FavoritesViewModel"
    }
}

class FavoritesViewModelFactory(private val memoRepository: MemoRepository)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(memoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")    }
}
