package com.aura.data.repository

import com.aura.core.media.LocalMusicDataSource
import com.aura.core.model.Song
import com.aura.domain.playback.LibraryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val localMusicDataSource: LocalMusicDataSource,
) : LibraryRepository {
    override suspend fun getSongs(): List<Song> = localMusicDataSource.loadSongs()

    override suspend fun getSongsByIds(ids: List<Long>): List<Song> =
        localMusicDataSource.findSongsByIds(ids)
}
