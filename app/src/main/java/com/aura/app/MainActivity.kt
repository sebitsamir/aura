package com.aura.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aura.app.navigation.AuraNavHost
import com.aura.core.designsystem.theme.AuraTheme
import com.aura.domain.playback.PlaybackRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var playbackRepository: PlaybackRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuraTheme {
                AuraNavHost(playbackRepository = playbackRepository)
            }
        }
    }
}