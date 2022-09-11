package com.thinkers.whiteboard.database.pagingsource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.thinkers.whiteboard.database.daos.MemoDao
import com.thinkers.whiteboard.database.entities.Memo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking


class MemoDataSource(private val memoDao: MemoDao): PagingSource<Int, Memo>() {
    override fun getRefreshKey(state: PagingState<Int, Memo>): Int? {
        Log.i(TAG,"in getRefreshKey")
        val anchorPosition = state.anchorPosition ?: return null
        Log.i(TAG,"anchorPosition: $anchorPosition")
        val memo = state.closestItemToPosition(anchorPosition) ?: return null
        Log.i(TAG,"memo: $memo")
        return getLeast(memo.memoId, state.config.pageSize)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Memo> =
        runBlocking(Dispatchers.IO) {
            Log.i(TAG,"params.loadSize: ${params.loadSize}, params.key: ${params.key}, params.placeholdersEnabled : ${params.placeholdersEnabled}")
            val nextPageNumber = params.key ?: 1
            Log.i(TAG,"nextPageNumber: $nextPageNumber")

            val pageMemos = memoDao.getPaginatedMemos(nextPageNumber, params.loadSize)
            Log.i(TAG, "$pageMemos")
            LoadResult.Page(
                data = pageMemos,
                prevKey = if (nextPageNumber == 1) null else nextPageNumber - 1,
                nextKey = if (pageMemos.isEmpty()) null else nextPageNumber + 1
            )
        }

    private fun getLeast(memoId: Int, pageSize: Int): Int {
        return if (memoId > pageSize / 2) memoId else pageSize / 2
    }

    companion object {
        val TAG = "MemoDataSource"
    }
}
