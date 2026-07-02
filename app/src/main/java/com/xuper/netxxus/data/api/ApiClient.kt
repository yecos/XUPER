package com.xuper.netxxus.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton cliente Retrofit para la API de Xuper Hydra.
 *
 * ⚠️ BASE_URL está vacío intencionalmente. Se completa después de capturar
 * el tráfico real con mitmproxy (ver docs/CAPTURA-TRAFICO.md).
 *
 * La app original usa un sistema de bootstrap:
 *  1. Llama al servidor DCS inicial (URL descifrada de strings.xml)
 *  2. Recibe la lista de URLs reales (portal, epg, ads, etc.)
 *  3. Usa esas URLs para todas las llamadas siguientes
 *
 * Para emular esto en el proyecto nuevo, ver `BootstrapManager.kt`.
 */
object ApiClient {

    /**
     * URL inicial del servidor DCS.
     * TODO: Pegar aquí la URL capturada con mitmproxy.
     */
    const val DCS_BOOTSTRAP_URL = ""

    /**
     * URL base del portal (se obtiene del DCS en runtime).
     */
    var portalBaseUrl: String = ""

    /**
     * URL base del EPG (se obtiene del DCS en runtime).
     */
    var epgBaseUrl: String = ""

    /**
     * Token de autenticación del usuario (después de login).
     */
    var authToken: String = ""

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .apply {
                        if (authToken.isNotEmpty()) {
                            addHeader("Authorization", "Bearer $authToken")
                        }
                        addHeader("User-Agent", "XuperHydra/4.35.0 (Android TV)")
                        addHeader("X-Device-Id", DeviceId.get())
                    }
                    .build()
                chain.proceed(req)
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: XuperApi by lazy {
        Retrofit.Builder()
            .baseUrl(portalBaseUrl.ifEmpty { "https://placeholder.example.com/" })
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XuperApi::class.java)
    }
}

/**
 * Genera / recupera un ID único de dispositivo (persistent across installs).
 */
object DeviceId {
    private const val PREFS = "xuper_prefs"
    private const val KEY = "device_id"

    fun get(): String {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(
            com.xuper.netxxus.XuperApp.instance
        )
        return prefs.getString(KEY, null) ?: run {
            val newId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString(KEY, newId).apply()
            newId
        }
    }
}
