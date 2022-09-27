package com.thinkers.whiteboard.common.memo

import kotlinx.coroutines.flow.Flow

interface MemoDataChangeInfoSender {
    fun getDataChangeInfo(updated: Boolean): Flow<Boolean>
}