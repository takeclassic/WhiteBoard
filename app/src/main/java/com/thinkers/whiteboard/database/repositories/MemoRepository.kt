package com.thinkers.whiteboard.database.repositories

import androidx.annotation.WorkerThread
import com.thinkers.whiteboard.database.daos.MemoDao
import com.thinkers.whiteboard.database.entities.Memo
import kotlinx.coroutines.flow.Flow

class MemoRepository(private val memoDao: MemoDao) {
    var noteName: String = "내 메모"

    val allMemos: Flow<List<Memo>> = memoDao.getAllMemos()

    val allFavoriteMemos: Flow<List<Memo>> = memoDao.getAllFavoriteMemos()

    fun getMemoById(id: Int): Flow<Memo> = memoDao.getMemo(id)

    fun getPaginatedMemos(page: Int, loadSize: Int) {

    }

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
}