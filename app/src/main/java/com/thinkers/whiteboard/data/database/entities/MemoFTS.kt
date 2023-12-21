package com.thinkers.whiteboard.data.database.entities

import androidx.room.*

@Entity(tableName = "memo_fts")
@Fts4(contentEntity = Memo::class, order = FtsOptions.Order.DESC)
data class MemoFTS(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int,
    @ColumnInfo(name = "text") var text: String
)
