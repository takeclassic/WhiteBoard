package com.thinkers.whiteboard.database.repositories

import android.text.Editable
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.thinkers.whiteboard.common.interfaces.MemoItemCache
import com.thinkers.whiteboard.common.memo.MemoDataChangeInfoSender
import com.thinkers.whiteboard.database.daos.MemoDao
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.pagingsource.DataSourceHolder
import com.thinkers.whiteboard.database.pagingsource.MemoDataSource
import com.thinkers.whiteboard.database.pagingsource.NoteAndMemosDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MemoRepository(private val memoDao: MemoDao): MemoItemCache {
    var allMemosCount = 0
    var allFavoriteMemosCount = 0
    var customMemosCount = 0
    var noteName: String = "내 메모"
    val allMemos: Flow<List<Memo>> = memoDao.getAllMemos()
    val allFavoriteMemos: Flow<List<Memo>> = memoDao.getAllFavoriteMemos()
    val dataSourceHolder: DataSourceHolder<MemoDataSource> = DataSourceHolder()
    val totalMemoCount: Flow<Int> = memoDao.getAllMemosCount()

    private val _newMemoState = MutableStateFlow(false) // private mutable state flow
    val newMemoState = _newMemoState.asStateFlow()

    private var lastCheckedMemo: Memo? = null
    private var position: Int = 0

    fun setAllCustomMemosCountByNoteName(noteName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            memoDao.getCustomMemosCountByNoteName(noteName).collectLatest {
                customMemosCount = it
            }
        }
    }

    fun getDataUpdated(isUpdated: Boolean) {
        _newMemoState.value = isUpdated
        if (isUpdated) {
            Log.i(TAG, "KKKKK updated")
            //dataSourceHolder.getDataSource().invalidate()
        }
    }

    fun getMemoById(id: Int): Flow<Memo> = memoDao.getMemo(id)

    @WorkerThread
    fun saveMemo(memo: Memo) {
        memoDao.insertMemo(memo)
    }

    @WorkerThread
    fun updateMemo(memo: Memo) {
        memoDao.updateMemo(memo)
    }

    @WorkerThread
    fun deleteMemo(memo: Memo) {
        memoDao.deleteMemo(memo)
    }

    fun getAllPagingMemos(): Flow<PagingData<Memo>> {
        return Pager(
            PagingConfig(
                initialLoadSize = 10,
                pageSize = 10,
                enablePlaceholders = false,
                maxSize = 100
            )
        ) {
            dataSourceHolder.create(MemoDataSource(memoDao, "total", "", position, allMemosCount))
            dataSourceHolder.getDataSource()
            //MemoDataSource(memoDao, "total", "")
        }.flow
    }

    fun getCustomPagingMemos(noteName: String): Flow<PagingData<Memo>> {
        return Pager(
            PagingConfig(
                initialLoadSize = 10,
                pageSize = 10,
                prefetchDistance = 10
            )
        ) {
            //NoteAndMemosDataSource(memoDao, noteName)
            dataSourceHolder.create(MemoDataSource(memoDao, "custom", noteName, position, customMemosCount))
            dataSourceHolder.getDataSource()
            //MemoDataSource(memoDao, "custom", noteName)
        }.flow
    }

    fun getFavoritePagingMemos(): Flow<PagingData<Memo>> {
        return Pager(
            PagingConfig(
                initialLoadSize = 10,
                pageSize = 10,
                prefetchDistance = 10
            )
        ) {
            dataSourceHolder.create(MemoDataSource(memoDao, "favorites", "", position, allFavoriteMemosCount))
            dataSourceHolder.getDataSource()
            //MemoDataSource(memoDao, "favorites", "")
        }.flow
    }

    fun getSearchingMemos(query: String): Flow<List<Memo>> {
        Log.i(TAG, "query: $query")
        return memoDao.getSearchingMemos(query)
    }

    fun getPaginatedMemos(pageNum: Int, pageSize: Int): Flow<List<Memo>> {
        return memoDao.getPaginatedMemoFlow(pageNum, pageSize)
    }

    override fun setLastCheckedMemo(memo: Memo, position: Int) {
        lastCheckedMemo = memo
        this.position = position
    }

    fun getLastCheckedMemoId(): Int {
        return lastCheckedMemo?.memoId ?: 0
    }
    companion object {
        val TAG = "MemoRepository"
    }
}
