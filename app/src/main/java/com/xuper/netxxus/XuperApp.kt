package com.xuper.netxxus

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.xuper.netxxus.ui.theme.XuperThemeHolder
import java.util.Locale

/**
 * Application class — reemplaza a `com.interactive.brasiliptv.app.AppWrapper` de la APK original.
 *
 * Aquí inicializamos:
 *  - Tema personalizable (desde assets/theme-config.json)
 *  - Idioma (auto-detección del sistema o el guardado por el usuario)
 *  - Tema oscuro por defecto (coherente con la paleta streaming-oscuro)
 *  - Cualquier librería global ( Retrofit, DataStore, etc. )
 */
class XuperApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Inicializar tema personalizable desde assets
        XuperThemeHolder.initialize(this)

        // Forzar modo oscuro (la paleta es streaming-oscuro por diseño)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        // Aplicar idioma guardado (o auto-detectar del sistema)
        applySavedLanguage()
    }

    private fun applySavedLanguage() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lang = prefs.getString(KEY_LANGUAGE, null) ?: // null = auto
            resources.configuration.locales[0].language

        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    companion object {
        lateinit var instance: XuperApp
            private set

        const val PREFS_NAME = "xuper_prefs"
        const val KEY_LANGUAGE = "language"  // null | "es" | "en"
    }
}
