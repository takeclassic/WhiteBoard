package com.thinkers.whiteboard.common.interfaces

import com.thinkers.whiteboard.database.entities.Memo

interface ActionModeDataHelper {
    fun removeItems(memoList: List<Memo>)
    fun moveItems(memoList: List<Memo>)
}