package com.thinkers.whiteboard.common.interfaces

import com.thinkers.whiteboard.database.entities.Memo

interface TotalPagingMemoListener {
    fun onMemoListUpdated(memoList: List<Memo>)
}