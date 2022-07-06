package com.thinkers.whiteboard.database.repositories

import androidx.annotation.WorkerThread
import com.thinkers.whiteboard.database.daos.MemoDao
import com.thinkers.whiteboard.database.entities.Memo
import kotlinx.coroutines.flow.Flow

class MemoRepository(private val memoDao: MemoDao) {
    val allMemos: Flow<List<Memo>> = memoDao.getAllMemos()

    fun getMemoById(id: Int): Flow<Memo> = memoDao.getMemo(id)

    @WorkerThread
    suspend fun saveMemo(memo: Memo) {
        memoDao.insertMemo(memo)
    }

    @WorkerThread
    suspend fun updateMemo(memo: Memo) {
        memoDao.updateMemo(memo)
    }

    @WorkerThread
    suspend fun deleteMemo(memo: Memo) {
        memoDao.deleteMemo(memo)
    }
}