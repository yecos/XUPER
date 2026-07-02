package com.xuper.netxxus.data.api

import com.xuper.netxxus.data.session.SessionInterceptor
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

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(SessionInterceptor())  // Auth + device headers
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
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
