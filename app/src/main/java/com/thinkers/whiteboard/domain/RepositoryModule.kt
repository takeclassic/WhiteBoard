package com.thinkers.whiteboard.domain

import com.thinkers.whiteboard.data.repositories.MemoRepositoryImpl
import com.thinkers.whiteboard.data.repositories.NoteRepositoryImpl
import com.thinkers.whiteboard.data.repositories.SettingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    abstract fun bindMemoRepository(impl: MemoRepositoryImpl): MemoRepository
    @Binds
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository
    @Binds
    abstract fun bindSettingRepository(impl: SettingRepositoryImpl): SettingRepository
}
