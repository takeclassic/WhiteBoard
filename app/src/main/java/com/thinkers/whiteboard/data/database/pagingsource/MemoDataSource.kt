package com.thinkers.whiteboard.data.database.pagingsource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.thinkers.whiteboard.data.database.daos.MemoDao
import com.thinkers.whiteboard.data.database.entities.Memo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.lang.Integer.max


class MemoDataSource(
    private val memoDao: MemoDao,
    private val key: String,
    private val noteName: String,
    private val memoPosition: Int,
    private val totalMemoCount: Int
    ): PagingSource<Int, Memo>() {
    private var itemBefore = 0

    override fun getRefreshKey(state: PagingState<Int, Memo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            Log.i(TAG, "prev: ${state.closestPageToPosition(memoPosition)?.prevKey}")
            Log.i(TAG, "next: ${state.closestPageToPosition(memoPosition)?.nextKey}")

            state.closestPageToPosition(memoPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(memoPosition)?.nextKey?.minus(1)
        }

    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Memo> =
        runBlocking(Dispatchers.IO) {
            var nextPageNumber = params.key ?: 1
            Log.i(TAG, "nextPageNumber: $nextPageNumber, memoPosition: $memoPosition")
            var loadSize = params.loadSize
            var pageMemos: List<Memo>? = null

            when (key) {
                "total" -> {
                    pageMemos = memoDao.getPaginatedMemos(nextPageNumber, loadSize)
                }
                "favorites" -> {
                    pageMemos = memoDao.getPaginatedFavoriteMemos(nextPageNumber, loadSize)
                }
                "custom" -> {
                    pageMemos = memoDao.getPaginatedMemosByNoteName(noteName, nextPageNumber, loadSize)
                }
            }

            LoadResult.Page(
                data = pageMemos ?: listOf(),
                prevKey = if (nextPageNumber == 1) null else nextPageNumber - 1,
                nextKey = if (pageMemos.isNullOrEmpty()) null else nextPageNumber + 1,
                itemsBefore = memoPosition
            )
        }

    companion object {
        val TAG = "MemoDataSource"
    }
}
