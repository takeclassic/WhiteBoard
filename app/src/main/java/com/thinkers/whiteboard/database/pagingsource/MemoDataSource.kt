package com.thinkers.whiteboard.database.pagingsource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.thinkers.whiteboard.database.daos.MemoDao
import com.thinkers.whiteboard.database.entities.Memo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking


class MemoDataSource(
    private val memoDao: MemoDao,
    private val key: String,
    private val noteName: String
    ): PagingSource<Int, Memo>() {
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
            var pageMemos: List<Memo>? = null
            when (key) {
                "total" -> {
                    pageMemos = memoDao.getPaginatedMemos(nextPageNumber, params.loadSize)
                }
                "favorites" -> {
                    pageMemos = memoDao.getPaginatedFavoriteMemos(nextPageNumber, params.loadSize)
                }
                "custom" -> {
                    pageMemos = memoDao.getPaginatedMemosByNotename(noteName, nextPageNumber, params.loadSize)
                }
            }
            Log.i(TAG, "$pageMemos")
            LoadResult.Page(
                data = pageMemos ?: listOf(),
                prevKey = if (nextPageNumber == 1) null else nextPageNumber - 1,
                nextKey = if (pageMemos.isNullOrEmpty()) null else nextPageNumber + 1
            )
        }

    companion object {
        val TAG = "MemoDataSource"
    }
}
