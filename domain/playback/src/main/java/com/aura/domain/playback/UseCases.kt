package com.aura.domain.playback

import com.aura.core.model.Song
import com.aura.core.playback.PlaybackCommand
import javax.inject.Inject

class GetLocalSongsUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository,
) {
    suspend operator fun invoke(): List<Song> = libraryRepository.getSongs()
}

class PlaySongsUseCase @Inject constructor(
    private val playbackRepository: PlaybackRepository,
) {
    suspend operator fun invoke(songs: List<Song>, startIndex: Int = 0) {
        playbackRepository.connect()
        playbackRepository.send(PlaybackCommand.PlayQueue(songs, startIndex))
    }
}

class SendPlaybackCommandUseCase @Inject constructor(
    private val playbackRepository: PlaybackRepository,
) {
    suspend operator fun invoke(command: PlaybackCommand) {
        playbackRepository.connect()
        playbackRepository.send(command)
    }
}

class ObservePlaybackStateUseCase @Inject constructor(
    private val playbackRepository: PlaybackRepository,
) {
    operator fun invoke() = playbackRepository.playbackState
}
