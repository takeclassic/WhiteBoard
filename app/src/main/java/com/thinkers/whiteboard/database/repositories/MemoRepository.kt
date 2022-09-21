package com.thinkers.whiteboard.database.repositories

import android.text.Editable
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.thinkers.whiteboard.database.daos.MemoDao
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.pagingsource.MemoDataSource
import com.thinkers.whiteboard.database.pagingsource.NoteAndMemosDataSource
import kotlinx.coroutines.flow.Flow

class MemoRepository(private val memoDao: MemoDao) {
    var noteName: String = "내 메모"

    val allMemos: Flow<List<Memo>> = memoDao.getAllMemos()

    val allFavoriteMemos: Flow<List<Memo>> = memoDao.getAllFavoriteMemos()

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
                prefetchDistance = 10
            )
        ) {
            MemoDataSource(memoDao, "total", "")
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
            MemoDataSource(memoDao, "custom", noteName)
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
            MemoDataSource(memoDao, "favorites", "")
        }.flow
    }

    fun getSearchingMemos(query: String): Flow<List<Memo>> {
        Log.i(TAG, "query: $query")
        return memoDao.getSearchingMemos(query)
    }

    companion object {
        val TAG = "MemoRepository"
    }

}
