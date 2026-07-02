package com.xuper.netxxus.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.xuper.netxxus.R
import com.xuper.netxxus.ui.theme.XuperHydraTheme

/**
 * Reproductor de vídeo fullscreen.
 * Reemplaza a `com.download.activity.LocalPlayActivity` (usando Media3 / ExoPlayer).
 *
 * TODO: integrar androidx.media3.PlayerView con ExoPlayer
 */
class PlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XuperHydraTheme {
                PlayerScreen()
            }
        }
    }
}

@Composable
private fun PlayerScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Text(
            text = stringResource(R.string.player_loading),
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(32.dp)
        )
        // TODO: reemplazar por:
        // val exoPlayer = remember { ExoPlayer.Builder(this).build() }
        // AndroidView(factory = { PlayerView(it).apply { player = exoPlayer } })
    }
}
