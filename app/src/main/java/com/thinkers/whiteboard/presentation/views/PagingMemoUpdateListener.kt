package com.thinkers.whiteboard.presentation.views

import com.thinkers.whiteboard.data.database.entities.Memo

interface PagingMemoUpdateListener {
    fun onMemoListUpdated(memoList: List<Memo>)
}
