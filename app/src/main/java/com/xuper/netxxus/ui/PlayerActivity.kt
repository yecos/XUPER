package com.xuper.netxxus.ui

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.xuper.netxxus.data.drm.DrmManager
import com.xuper.netxxus.data.session.SessionManager
import com.xuper.netxxus.ui.theme.XuperHydraTheme
import com.xuper.netxxus.ui.theme.XuperRed

/**
 * Reproductor de vídeo fullscreen con soporte DRM Widevine.
 *
 * Reemplaza a `com.download.activity.LocalPlayActivity` de la APK original.
 *
 * Flujo de reproducción:
 *  1. Recibe por Intent: stream URL + license URL (del VodDetails o LiveChannel)
 *  2. Crea Media3 ExoPlayer con DrmSessionManager si el contenido es protegido
 *  3. Reproduce HLS o MPEG-DASH según la extensión de la URL
 *
 * ⚠️ No hay bypass DRM. Si el usuario no tiene sesión válida o el dispositivo
 * está bloqueado por el license server, la reproducción falla (comportamiento correcto).
 */
class PlayerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_STREAM_URL = "stream_url"
        const val EXTRA_LICENSE_URL = "license_url"
        const val EXTRA_TITLE = "title"
        const val EXTRA_CONTENT_TYPE = "content_type"  // "vod" | "live"
    }

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val streamUrl = intent.getStringExtra(EXTRA_STREAM_URL) ?: ""
        val licenseUrl = intent.getStringExtra(EXTRA_LICENSE_URL) ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val contentType = intent.getStringExtra(EXTRA_CONTENT_TYPE) ?: "vod"

        setContent {
            XuperHydraTheme {
                PlayerScreen(
                    streamUrl = streamUrl,
                    licenseUrl = licenseUrl,
                    title = title,
                    contentType = contentType,
                    onBack = { finish() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun PlayerScreen(
    streamUrl: String,
    licenseUrl: String,
    title: String,
    contentType: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Crear ExoPlayer una sola vez
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> isLoading = true
                        Player.STATE_READY -> {
                            isLoading = false
                            errorMessage = null
                        }
                        Player.STATE_ENDED -> isLoading = false
                        Player.STATE_IDLE -> isLoading = false
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    isLoading = false
                    errorMessage = when (error.errorCode) {
                        androidx.media3.common.PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED ->
                            "Error de licencia DRM. Tu cuenta o dispositivo no tiene permiso para este contenido."
                        androidx.media3.common.PlaybackException.ERROR_CODE_DRM_DISALLOWED_OPERATION ->
                            "Operación DRM no permitida en este dispositivo."
                        androidx.media3.common.PlaybackException.ERROR_CODE_DRM_SYSTEM_ERROR ->
                            "Error del sistema DRM: ${error.message}"
                        androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                            "Error de red al cargar el stream."
                        else -> "Error de reproducción: ${error.message}"
                    }
                }

                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
            })
        }
    }

    // Configurar source cuando cambia la URL
    LaunchedEffect(streamUrl, licenseUrl) {
        if (streamUrl.isNotBlank()) {
            val mediaItemBuilder = MediaItem.Builder()
                .setUri(streamUrl)

            // Configurar DRM si hay license URL (Widevine)
            if (licenseUrl.isNotBlank()) {
                // API Media3 1.4: DrmConfiguration.Builder() es privado,
                // se construye vía MediaItem.Builder().setDrmConfiguration(lambda)
                mediaItemBuilder.setDrmConfiguration(
                    androidx.media3.common.MediaItem.DrmConfiguration.Builder(licenseUrl).build()
                )
            }

            exoPlayer.setMediaItem(mediaItemBuilder.build())
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    // Limpiar al salir del composable
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // PlayerView (de Media3 UI)
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = exoPlayer
                    useController = true
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top bar con título y botón back
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp
                )
                if (contentType == "live") {
                    Text(
                        text = "EN VIVO",
                        color = XuperRed,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Loading indicator
        if (isLoading && errorMessage == null) {
            CircularProgressIndicator(
                color = XuperRed,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Error message
        if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.85f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error de reproducción",
                        color = XuperRed,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = XuperRed)
                    ) {
                        Text("Volver", color = Color.White)
                    }
                }
            }
        }

        // DRM info badge (esquina inferior derecha)
        if (licenseUrl.isNotBlank() && errorMessage == null) {
            val secLevel = remember { DrmManager.getSecurityLevel() }
            Text(
                text = "Widevine $secLevel",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }
    }
}
