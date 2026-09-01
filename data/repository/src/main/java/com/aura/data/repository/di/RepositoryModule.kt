package com.aura.data.repository.di

import com.aura.data.repository.LibraryRepositoryImpl
import com.aura.data.repository.PlaybackRepositoryImpl
import com.aura.domain.playback.LibraryRepository
import com.aura.domain.playback.PlaybackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository

    @Binds
    @Singleton
    abstract fun bindPlaybackRepository(impl: PlaybackRepositoryImpl): PlaybackRepository
}
