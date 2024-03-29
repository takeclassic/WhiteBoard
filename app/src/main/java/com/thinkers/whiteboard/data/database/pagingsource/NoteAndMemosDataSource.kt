package com.thinkers.whiteboard.data.database.pagingsource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.thinkers.whiteboard.data.database.daos.MemoDao
import com.thinkers.whiteboard.data.database.entities.Memo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class NoteAndMemosDataSource(
    private val memoDao: MemoDao,
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
            val memoList = memoDao.getPaginatedMemosByNoteName(noteName, nextPageNumber, params.loadSize)

            LoadResult.Page(
                data = memoList,
                prevKey = if (nextPageNumber == 1) null else nextPageNumber - 1,
                nextKey = if (memoList.isNullOrEmpty()) null else nextPageNumber + 1
            )
        }

    companion object {
        val TAG = "NoteAndMemosDataSource"
    }
}