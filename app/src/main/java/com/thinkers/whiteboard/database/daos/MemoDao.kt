package com.thinkers.whiteboard.database.daos

import androidx.room.*
import com.thinkers.whiteboard.database.entities.Memo
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    @Query("SELECT * FROM memo")
    fun getAllMemos(): Flow<List<Memo>>

    @Query("SELECT * FROM memo WHERE is_favorite LIKE 1")
    fun getAllFavoriteMemos(): Flow<List<Memo>>

    @Query("SELECT * FROM memo WHERE memo_id LIKE (:memoId)")
    fun getMemo(memoId: Int): Flow<Memo>

    @Insert
    fun insertMemo(memo: Memo)

    @Update
    fun updateMemo(memo: Memo)

    @Delete
    fun deleteMemo(memo: Memo)
}