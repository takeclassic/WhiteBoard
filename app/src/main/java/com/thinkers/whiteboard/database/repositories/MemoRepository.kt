package com.thinkers.whiteboard.database.repositories

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.thinkers.whiteboard.common.DispatcherProvider
import com.thinkers.whiteboard.common.enums.MemoUpdateState
import com.thinkers.whiteboard.database.daos.MemoDao
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.pagingsource.DataSourceHolder
import com.thinkers.whiteboard.database.pagingsource.MemoDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class MemoRepository(
    private val memoDao: MemoDao,
    private val dispatchers: DispatcherProvider
) {
    var allMemosCount = 0
    var allFavoriteMemosCount = 0
    var customMemosCount = 0
    var noteName: String = "내 메모"
    val allMemos: Flow<List<Memo>> = memoDao.getAllMemos()
    val allFavoriteMemos: Flow<List<Memo>> = memoDao.getAllFavoriteMemos()
    val dataSourceHolder: DataSourceHolder<MemoDataSource> = DataSourceHolder()
    val totalMemoCount: Flow<Int> = memoDao.getAllMemosCount()
    val favoritesMemoCount: Flow<Int> = memoDao.getFavoriteMemosCount()

    var memoState = MemoUpdateState.NONE
    var updatedMemo: Memo = Memo(-1, "", 0,0, 0,"")

    private var lastCheckedMemo: Memo? = null
    private var position: Int = 0

    fun setAllCustomMemosCountByNoteName(noteName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            memoDao.getCustomMemosCountByNoteName(noteName).collectLatest {
                customMemosCount = it
            }
        }
    }

    fun getDataUpdated(memo: Memo, state: MemoUpdateState) {
        Log.i(TAG, "repo, state: $state")
        updatedMemo = memo
        memoState = state
    }

    fun getMemoById(id: Int): Flow<Memo> = memoDao.getMemo(id)

    @WorkerThread
    suspend fun saveMemo(memo: Memo) = withContext(dispatchers.io) {
        memoDao.insertMemo(memo)
    }

    @WorkerThread
    suspend fun updateMemo(memo: Memo) = withContext(dispatchers.io) {
        memoDao.updateMemo(memo)
    }

    @WorkerThread
    suspend fun deleteMemo(memo: Memo) = withContext(dispatchers.io) {
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

    suspend fun getSearchingMemos(query: String): List<Memo> = withContext(dispatchers.io) {
        val value = memoDao.getSearchingMemos(query)
        Log.i(TAG, "query: $query, value: $value")
        value
    }

    fun getPaginatedMemoFlow(pageNum: Int, pageSize: Int): Flow<List<Memo>> {
        return memoDao.getPaginatedMemoFlow(pageNum, pageSize)
    }

    suspend fun getPaginatedMemoList(pageNum: Int, pageSize: Int): List<Memo> = withContext(dispatchers.io) {
        memoDao.getPaginatedMemos(pageNum, pageSize)
    }

    suspend fun getPaginatedFavoriteMemoList(pageNum: Int, pageSize: Int): List<Memo> = withContext(dispatchers.io) {
        memoDao.getPaginatedFavoriteMemos(pageNum, pageSize)
    }

    suspend fun getPaginatedMemosByNoteName(noteName: String, pageNum: Int, pageSize: Int): List<Memo> = withContext(dispatchers.io) {
        memoDao.getPaginatedMemosByNoteName(noteName, pageNum, pageSize)
    }

    fun getLastCheckedMemoId(): Int {
        return lastCheckedMemo?.memoId ?: 0
    }

    fun getCustomNoteMemoCount(noteName: String): Flow<Int> {
        return memoDao.getCustomMemosCountByNoteName(noteName)
    }

    companion object {
        val TAG = "MemoRepository"
    }
}
