package com.thinkers.whiteboard.favorites

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thinkers.whiteboard.common.interfaces.PagingMemoUpdateListener
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.repositories.MemoRepository
import com.thinkers.whiteboard.total.TotalFragment
import com.thinkers.whiteboard.total.TotalViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val memoRepository: MemoRepository,
    private val memoListUpdateListener: PagingMemoUpdateListener
) : ViewModel() {
    val allFavorites: LiveData<List<Memo>> = memoRepository.allFavoriteMemos.asLiveData()

    private var _memoList = mutableListOf<Memo>()
    val memoList: List<Memo> = _memoList

    val memoMap = mutableMapOf<Int, Int>()
    var memoToUpdate: Memo = Memo(-1, "", 0,0, "")

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

    fun initKeepUpdated() {
        viewModelScope.launch(Dispatchers.IO) {
            memoRepository.newMemoState.collectLatest { updatedMemo ->
                Log.i(TAG, "updatedMemo: ${updatedMemo.memoId}")
                memoToUpdate = updatedMemo
            }
        }
    }

    fun getNextPage(pageNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = memoRepository
                .getPaginatedFavoriteMemoList(pageNumber + 1, FavoritesFragment.PAGE_SIZE)
                .filter { !memoMap.containsKey(it.memoId) }

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
            memoListUpdateListener.onMemoListUpdated(memoList)
        }
    }

    companion object {
        const val TAG = "FavoritesViewModel"
    }
}

class FavoritesViewModelFactory(
    private val memoRepository: MemoRepository,
    private val memoListUpdateListener: PagingMemoUpdateListener)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(memoRepository, memoListUpdateListener) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")    }
}
