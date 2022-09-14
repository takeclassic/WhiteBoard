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
        return state.anchorPosition?.let { achorPosition ->
            Log.i(TAG,"achorPosition $achorPosition")
            state.closestPageToPosition(achorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(achorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Memo> =
        runBlocking(Dispatchers.IO) {
            val nextPageNumber = params.key ?: 1
            Log.i(TAG, "nextPageNumber: $nextPageNumber")
            val pageMemos = memoDao.getPaginatedMemos(nextPageNumber, params.loadSize)
            Log.i(TAG, "$pageMemos")
            LoadResult.Page(
                data = pageMemos,
                prevKey = if (nextPageNumber == 1) null else nextPageNumber - 1,
                nextKey = if (pageMemos.isEmpty()) null else nextPageNumber + 1
            )
        }

    companion object {
        val TAG = "MemoDataSource"
    }
}
