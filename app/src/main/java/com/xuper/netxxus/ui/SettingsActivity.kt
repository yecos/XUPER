package com.xuper.netxxus.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.xuper.netxxus.R
import com.xuper.netxxus.XuperApp
import com.xuper.netxxus.ui.theme.XuperHydraTheme
import com.xuper.netxxus.ui.theme.XuperRed
import androidx.compose.ui.unit.dp

/**
 * Pantalla de ajustes.
 * Incluye el selector de idioma (ES/EN/Auto).
 */
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XuperHydraTheme {
                SettingsScreen()
            }
        }
    }
}

@Composable
private fun SettingsScreen() {
    val ctx = LocalContext.current
    val prefs = ctx.getSharedPreferences(XuperApp.PREFS_NAME, Context.MODE_PRIVATE)
    val currentLang = prefs.getString(XuperApp.KEY_LANGUAGE, "auto") ?: "auto"
    var selected by remember { mutableStateOf(currentLang) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp)
    ) {
        Text(stringResource(R.string.nav_settings), color = Color.White, fontSize = 32.sp)
        Spacer(Modifier.height(24.dp))

        Text(stringResource(R.string.settings_language),
             color = Color.White, fontSize = 20.sp,
             modifier = Modifier.padding(bottom = 12.dp))

        listOf("auto" to R.string.settings_language_auto,
               "es"   to R.string.settings_language_es,
               "en"   to R.string.settings_language_en).forEach { (code, labelRes) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == code,
                    onClick = {
                        selected = code
                        val editor = prefs.edit()
                        if (code == "auto") {
                            editor.remove(XuperApp.KEY_LANGUAGE)
                        } else {
                            editor.putString(XuperApp.KEY_LANGUAGE, code)
                        }
                        editor.apply()
                        // Recrear la actividad para aplicar el idioma
                        (ctx as? ComponentActivity)?.recreate()
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = XuperRed)
                )
                Text(stringResource(labelRes), color = Color.White,
                     modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(Modifier.height(32.dp))
        TextButton(onClick = {}) {
            Text(stringResource(R.string.settings_about),
                 color = XuperRed, fontSize = 18.sp)
        }
    }
}
