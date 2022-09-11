package com.thinkers.whiteboard.database.repositories

import androidx.annotation.WorkerThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.thinkers.whiteboard.database.daos.MemoDao
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.pagingsource.MemoDataSource
import kotlinx.coroutines.flow.Flow

class MemoRepository(private val memoDao: MemoDao) {
    val memoDataSource = MemoDataSource(memoDao)

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

    fun getPagingMemos(): Flow<PagingData<Memo>> {
        return Pager(
            PagingConfig(
                initialLoadSize = 10,
                pageSize = 10,
                prefetchDistance = 10
            )
        ) {
            //memoDao.getPagingAllMemos()
            MemoDataSource(memoDao)
        }.flow
    }
}
