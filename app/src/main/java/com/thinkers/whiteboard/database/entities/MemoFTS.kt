package com.thinkers.whiteboard.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

@Entity(tableName = "memo_fts")
@Fts4(contentEntity = Memo::class, order = FtsOptions.Order.ASC)
data class MemoFTS(
    @ColumnInfo(name = "text") var text: String
)
