package com.xuper.netxxus.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xuper.netxxus.R
import com.xuper.netxxus.ui.theme.XuperHydraTheme
import com.xuper.netxxus.ui.theme.XuperRed

/**
 * Detalle de película/serie (VOD).
 * Reemplaza a `com.vod.ui.activity.VodDetailsActivity`.
 */
class VodDetailsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XuperHydraTheme {
                VodDetailsScreen()
            }
        }
    }
}

@Composable
private fun VodDetailsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp)
    ) {
        Text(
            text = "Título del contenido",
            color = Color.White,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "2024 · 1h 52min · Acción, Drama",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Descripción sinóptica del contenido seleccionado. Aquí se mostrará la sinopsis completa con detalles de la trama, reparto y dirección.",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 16.dp)
        )

        Row(
            modifier = Modifier.padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { /* TODO: iniciar reproductor */ },
                colors = ButtonDefaults.buttonColors(containerColor = XuperRed)
            ) {
                Text(stringResource(R.string.vod_play), color = Color.White)
            }
            OutlinedButton(onClick = {}) {
                Text(stringResource(R.string.vod_trailer), color = Color.White)
            }
            OutlinedButton(onClick = {}) {
                Text(stringResource(R.string.vod_add_to_list), color = Color.White)
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.vod_episodes),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )

        repeat(6) { i ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    "Episodio ${i + 1} — Título",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
