package com.thinkers.whiteboard.common.interfaces

import com.thinkers.whiteboard.database.entities.Memo

interface PagingMemoUpdateListener {
    fun onMemoListUpdated(memoList: List<Memo>)
}
