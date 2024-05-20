package com.thinkers.whiteboard.data.repositories

import android.content.Context
import androidx.annotation.WorkerThread
import com.google.firebase.annotations.PublicApi
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.data.enums.MemoUpdateState
import com.thinkers.whiteboard.data.database.daos.MemoDao
import com.thinkers.whiteboard.data.database.entities.Memo
import com.thinkers.whiteboard.domain.MemoRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoRepositoryImpl @Inject constructor(
    @Inject @JvmField var memoDao: MemoDao
): MemoRepository {
    companion object {
        val TAG = "MemoRepositoryImpl"
    }

    override var allMemosCount = 0
    override var allFavoriteMemosCount = 0
    override var customMemosCount = 0
    override var noteName: String = "my_memo"

    override var memoState: MemoUpdateState = MemoUpdateState.NONE
    override var updatedMemo: Memo = Memo(-1, "", 0,0, 0,"")

    private var lastCheckedMemo: Memo? = null
    private var position: Int = 0

    override fun getAllMemos(): Flow<List<Memo>> = memoDao.getAllMemos()

    override fun getAllFavortieMemos(): Flow<List<Memo>> = memoDao.getAllFavoriteMemos()

    override fun getTotalMemoCount(): Flow<Int> = memoDao.getAllMemosCount()

    override fun getFavoritesMemoCount(): Flow<Int> = memoDao.getFavoriteMemosCount()

    override fun setAllCustomMemosCountByNoteName(noteName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            memoDao.getCustomMemosCountByNoteName(noteName).collectLatest {
                customMemosCount = it
            }
        }
    }

    override fun getDataUpdated(memo: Memo, state: MemoUpdateState) {
        updatedMemo = memo
        memoState = state
    }

    override fun getMemoById(id: Int): Flow<Memo> = memoDao.getMemo(id)

    @WorkerThread
    override fun saveMemo(memo: Memo) {
        CoroutineScope(Dispatchers.IO).launch {
            memoDao.insertMemo(memo)
        }
    }

    @WorkerThread
    override fun updateMemo(memo: Memo) {
        CoroutineScope(Dispatchers.IO).launch {
            memoDao.updateMemo(memo)
        }
    }

    @WorkerThread
    override suspend fun deleteMemo(memo: Memo) = withContext(Dispatchers.IO) {
        memoDao.deleteMemo(memo)
    }

    @WorkerThread
    override suspend fun deleteAllMemos() = withContext(Dispatchers.IO) {
        memoDao.deleteAllMemos()
    }

    @WorkerThread
    override fun removeMemoToBin(memo: Memo) {
        CoroutineScope(Dispatchers.IO).launch {
            memo.noteName = "waste_bin"
            memoDao.updateMemo(memo)
        }
    }

    override suspend fun getSearchingMemos(query: String): List<Memo> = withContext(Dispatchers.IO) {
        val value = memoDao.getSearchingMemos(query)
        value
    }

    override fun getPaginatedMemoFlow(pageNum: Int, pageSize: Int) = memoDao.getPaginatedMemoFlow(pageNum, pageSize)

    override suspend fun getPaginatedMemoList(pageNum: Int, pageSize: Int) = withContext(Dispatchers.IO) {
        memoDao.getPaginatedMemos(pageNum, pageSize)
    }

    override suspend fun getPaginatedFavoriteMemoList(pageNum: Int, pageSize: Int) = withContext(Dispatchers.IO) {
        memoDao.getPaginatedFavoriteMemos(pageNum, pageSize)
    }

    override suspend fun getPaginatedMemosByNoteName(noteName: String, pageNum: Int, pageSize: Int) = withContext(Dispatchers.IO) {
        memoDao.getPaginatedMemosByNoteName(noteName, pageNum, pageSize)
    }

    override fun getLastCheckedMemoId() = lastCheckedMemo?.memoId ?: 0

    override fun getCustomNoteMemoCount(noteName: String): Flow<Int> = memoDao.getCustomMemosCountByNoteName(noteName)

    override fun getAllMemosWithoutFavorites(): List<Memo>  = memoDao.getAllMemosWithoutFavorites()

    override fun writeBooleanPreference(fileName: String, key: String, value: Boolean) {
        val context = WhiteBoardApplication.context()
        val sharedPref = context.getSharedPreferences(fileName, Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    override fun readBooleanPreference(fileName: String, key: String, defaultValue: Boolean): Boolean {
        val context = WhiteBoardApplication.context()
        val sharedPref =
            context.getSharedPreferences(fileName, Context.MODE_PRIVATE) ?: return defaultValue
        return sharedPref.getBoolean(key, defaultValue)
    }
}
