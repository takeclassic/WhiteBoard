package com.thinkers.whiteboard.common.interfaces

import com.thinkers.whiteboard.database.entities.Memo

interface MemoItemCache {
    fun setLastCheckedMemo(memo: Memo, position: Int)
}