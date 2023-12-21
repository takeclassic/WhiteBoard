package com.thinkers.whiteboard.data.database.daos

import androidx.paging.PagingSource
import androidx.room.*
import com.thinkers.whiteboard.data.database.entities.Memo
import com.thinkers.whiteboard.data.database.entities.NoteAndMemos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Dao
interface MemoDao {
    @Query("SELECT * FROM memo")
    fun getAllMemos(): Flow<List<Memo>>

    @Query("SELECT * FROM memo WHERE is_favorite LIKE 0 AND note_name NOT IN ('waste_bin')")
    fun getAllMemosWithoutFavorites(): List<Memo>

    @Query("SELECT * FROM memo ORDER BY created_time DESC")
    fun getPagingAllMemos(): PagingSource<Int, Memo>

    @Query("SELECT * FROM memo WHERE is_favorite LIKE 1")
    fun getAllFavoriteMemos(): Flow<List<Memo>>

    @Query("SELECT * FROM memo WHERE is_favorite LIKE 1 " +
            "ORDER BY created_time DESC " +
            "LIMIT :loadSize OFFSET (:page-1) * :loadSize")
    fun getPaginatedFavoriteMemos(page: Int, loadSize: Int): List<Memo>

    @Query("SELECT * FROM memo WHERE memo_id LIKE (:memoId)")
    fun getMemo(memoId: Int): Flow<Memo>

    @Query("SELECT * FROM memo ORDER BY created_time DESC LIMIT :loadSize OFFSET (:page-1) * :loadSize")
    fun getPaginatedMemos(page: Int, loadSize: Int): List<Memo>

    @Query("SELECT * FROM memo ORDER BY created_time DESC LIMIT :loadSize OFFSET (:page-1) * :loadSize")
    fun getPaginatedMemoFlow(page: Int, loadSize: Int): Flow<List<Memo>>

    @Transaction
    @Query("SELECT memo.* FROM memo " +
            "LEFT JOIN note ON memo.note_name = note.note_name " +
            "WHERE memo.note_name = :noteName ORDER BY created_time DESC " +
            "LIMIT :loadSize OFFSET (:page-1) * :loadSize")
    fun getPaginatedMemosByNoteName(noteName: String, page: Int, loadSize: Int): List<Memo>

    @Transaction
    @Query("SELECT memo.* FROM " +
            "(SELECT rowid, * FROM memo_fts WHERE memo_fts MATCH :query) AS fts, memo " +
            "WHERE fts.rowid = memo.rowid")
    fun getSearchingMemos(query: String): List<Memo>

    @Query("SELECT COUNT(*) FROM memo WHERE note_name NOT IN ('waste_bin')")
    fun getAllMemosCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM memo WHERE is_favorite LIKE 1")
    fun getFavoriteMemosCount(): Flow<Int>

    @Transaction
    @Query("SELECT COUNT(*) FROM memo " +
            "LEFT JOIN note ON memo.note_name = note.note_name " +
            "WHERE memo.note_name = :noteName")
    fun getCustomMemosCountByNoteName(noteName: String): Flow<Int>

    @Insert
    fun insertMemo(memo: Memo)

    @Update
    fun updateMemo(memo: Memo)

    @Delete
    fun deleteMemo(memo: Memo)

    @Query("DELETE FROM memo WHERE memo.note_name = 'waste_bin'")
    fun deleteAllMemos()
}
