package com.thinkers.whiteboard.database.daos

import androidx.paging.PagingSource
import androidx.room.*
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.database.entities.NoteAndMemos
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    @Query("SELECT * FROM memo")
    fun getAllMemos(): Flow<List<Memo>>

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

    @Transaction
    @Query("SELECT memo.* FROM memo " +
            "LEFT JOIN note ON memo.note_name = note.note_name " +
            "WHERE memo.note_name = :noteName ORDER BY created_time DESC " +
            "LIMIT :loadSize OFFSET (:page-1) * :loadSize")
    fun getPaginatedMemosByNotename(noteName: String, page: Int, loadSize: Int): List<Memo>

    @Query("SELECT memo.* FROM memo " +
            "JOIN memo_fts ON memo.text = memo_fts.text " +
            "WHERE memo_fts MATCH :query")
    fun getSearchingMemos(query: String): Flow<List<Memo>>

    @Insert
    fun insertMemo(memo: Memo)

    @Update
    fun updateMemo(memo: Memo)

    @Delete
    fun deleteMemo(memo: Memo)
}
