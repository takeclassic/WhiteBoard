package com.thinkers.whiteboard.database.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.thinkers.whiteboard.database.daos.MemoDao
import com.thinkers.whiteboard.database.entities.Memo

class MemoDataSource(private val memoDao: MemoDao): PagingSource<Int, Memo>() {
    override fun getRefreshKey(state: PagingState<Int, Memo>): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Memo> {
        val nextPageNumber = params.key ?: 1

        val pageMemos = memoDao.getPaginatedMemos(nextPageNumber, params.loadSize)
    }


}