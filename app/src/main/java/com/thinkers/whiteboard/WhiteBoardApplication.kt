package com.thinkers.whiteboard

import android.app.Application
import com.thinkers.whiteboard.database.AppDatabase
import com.thinkers.whiteboard.database.repositories.MemoRepository
import com.thinkers.whiteboard.database.repositories.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class WhiteBoardApplication: Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val memoRepository by lazy { MemoRepository(database.memoDao()) }
    val noteRepository by lazy { NoteRepository(database.noteDao()) }
}
