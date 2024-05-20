package com.thinkers.whiteboard.domain

import com.thinkers.whiteboard.data.database.entities.Memo
import com.thinkers.whiteboard.data.enums.MemoUpdateState
import kotlinx.coroutines.flow.Flow

interface MemoRepository {
    var allMemosCount: Int
    var allFavoriteMemosCount: Int
    var customMemosCount: Int
    var noteName: String
    var memoState: MemoUpdateState
    var updatedMemo: Memo

    fun getAllMemos(): Flow<List<Memo>>

    fun getAllFavortieMemos(): Flow<List<Memo>>

    fun getTotalMemoCount(): Flow<Int>

    fun getFavoritesMemoCount(): Flow<Int>

    fun setAllCustomMemosCountByNoteName(noteName: String)

    fun getDataUpdated(memo: Memo, state: MemoUpdateState)

    fun getMemoById(id: Int): Flow<Memo>

    fun saveMemo(memo: Memo)

    fun updateMemo(memo: Memo)

    suspend fun deleteMemo(memo: Memo)

    suspend fun deleteAllMemos()

    fun removeMemoToBin(memo: Memo)

    suspend fun getSearchingMemos(query: String): List<Memo>

    fun getPaginatedMemoFlow(pageNum: Int, pageSize: Int): Flow<List<Memo>>

    suspend fun getPaginatedMemoList(pageNum: Int, pageSize: Int): List<Memo>

    suspend fun getPaginatedFavoriteMemoList(pageNum: Int, pageSize: Int): List<Memo>

    suspend fun getPaginatedMemosByNoteName(noteName: String, pageNum: Int, pageSize: Int): List<Memo>

    fun getLastCheckedMemoId(): Int

    fun getCustomNoteMemoCount(noteName: String): Flow<Int>

    fun getAllMemosWithoutFavorites(): List<Memo>

    fun writeBooleanPreference(fileName: String, key: String, value: Boolean)

    fun readBooleanPreference(fileName: String, key: String, defaultValue: Boolean): Boolean
}