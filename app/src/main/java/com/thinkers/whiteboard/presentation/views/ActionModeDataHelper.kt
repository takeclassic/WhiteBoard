package com.thinkers.whiteboard.presentation.views

import com.thinkers.whiteboard.data.database.entities.Memo

interface ActionModeDataHelper {
    fun removeItems(memoList: List<Memo>)
    fun moveItems(memoList: List<Memo>)
}