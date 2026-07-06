package com.xuper.netxxus.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xuper.netxxus.R
import com.xuper.netxxus.ui.theme.ThemeConfig
import com.xuper.netxxus.ui.theme.XuperHydraTheme
import com.xuper.netxxus.ui.theme.xuperColors

/** Actividad base que carga el tema desde assets/theme-config.json */
abstract class BaseThemeActivity : ComponentActivity() {
    protected lateinit var themeConfig: ThemeConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        themeConfig = ThemeConfig.fromAssets(this)
        super.onCreate(savedInstanceState)
    }
}

/**
 * Pantalla principal (Home).
 * Reemplaza a `com.main.ui.activity.HomeActivity` de la APK original.
 *
 * Estructura:
 *  - Top bar lateral con logo + nav
 *  - Carruseles horizontales (Live, Películas, Series, etc.)
 *  - Banner superior con destacado
 */
class HomeActivity : BaseThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XuperHydraTheme(this) {
                HomeScreen()
            }
        }
    }
}

@Composable
private fun HomeScreen() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sidebar de navegación (estilo Android TV)
        NavSidebar()

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Banner destacado
            FeaturedBanner(
                title = "Bienvenido a Xuper Hydra",
                subtitle = "Miles de películas, series y canales en vivo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Carruseles
            ContentRow(title = stringResource(R.string.home_category_new), items = mockItems())
            Spacer(Modifier.height(20.dp))
            ContentRow(title = stringResource(R.string.home_category_top), items = mockItems())
            Spacer(Modifier.height(20.dp))
            ContentRow(title = stringResource(R.string.home_category_show), items = mockItems())
        }
    }
}

@Composable
private fun NavSidebar() {
    Column(
        modifier = Modifier
            .width(140.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Logo
        Text(
            "XUPER",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )
        Text(
            "HYDRA",
            color = XuperRed,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 4.sp
        )

        Spacer(Modifier.height(20.dp))

        NavItem(icon = Icons.Filled.Tv, label = stringResource(R.string.nav_home))
        NavItem(icon = Icons.Filled.LiveTv, label = stringResource(R.string.nav_live))
        NavItem(icon = Icons.Filled.Movie, label = stringResource(R.string.nav_movies))
        NavItem(icon = Icons.Filled.Search, label = stringResource(R.string.nav_search))
        NavItem(icon = Icons.Filled.Person, label = stringResource(R.string.nav_mine))
        NavItem(icon = Icons.Filled.Settings, label = stringResource(R.string.nav_settings))
    }
}

@Composable
private fun NavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun FeaturedBanner(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        XuperRed.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(32.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Button(
                onClick = { /* TODO: navegar a player */ },
                colors = ButtonDefaults.buttonColors(containerColor = XuperRed),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(stringResource(R.string.vod_play), color = Color.White)
            }
        }
    }
}

@Composable
private fun ContentRow(title: String, items: List<String>) {
    Column {
        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier
                        .size(width = 160.dp, height = 220.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(item, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

private fun mockItems() = listOf(
    "Película 1", "Serie A", "Anime X", "Documental", "Live Match",
    "Película 2", "Serie B", "Anime Y"
)
