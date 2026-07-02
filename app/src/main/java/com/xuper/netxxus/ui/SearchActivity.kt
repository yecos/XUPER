package com.xuper.netxxus.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.xuper.netxxus.R
import com.xuper.netxxus.ui.theme.XuperHydraTheme
import androidx.compose.ui.unit.dp

/**
 * Pantalla de búsqueda.
 * Reemplaza a `com.vod.ui.activity.VodSearchActivity`.
 */
class SearchActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XuperHydraTheme {
                SearchScreen()
            }
        }
    }
}

@Composable
private fun SearchScreen() {
    val results = listOf("Resultado 1", "Resultado 2", "Resultado 3", "Resultado 4")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Text(stringResource(R.string.nav_search), color = Color.White, fontSize = 32.sp)
        Text(
            "${results.size} resultados",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyColumn {
            items(results) { r ->
                Text(r, color = Color.White, fontSize = 18.sp,
                     modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}
