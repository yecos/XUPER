package com.xuper.netxxus.data.drm

import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback
import java.util.UUID

/**
 * Gestiona DRM para los streams de Xuper Hydra.
 *
 * ⚠️ IMPORTANTE: Esto **NO** es un bypass de DRM. Es la integración LEGÍTIMA
 * con Widevine usando Media3 / ExoPlayer. El contenido solo se reproduce si:
 *  1. El usuario tiene sesión válida
 *  2. El backend retorna la license URL del contenido
 *  3. El servidor de licencias valida el dispositivo y emite la key
 *
 * La APK original usa IJKPlayer + ExoPlayer internamente. Para el proyecto
 * nuevo usamos Media3 (ExoPlayer 1.4+) que es el estándar moderno.
 *
 * Flujo DRM real de Xuper:
 *  1. La app pide al portal la URL del stream + license URL
 *  2. ExoPlayer crea una DefaultDrmSessionManager con la license URL
 *  3. Al cargar el manifiesto (HLS/DASH), ExoPlayer detecta el contenido protegido
 *  4. La sesión DRM contacta el license server enviando el Widevine device ID
 *  5. El license server valida contra el portal y entrega la key
 *  6. ExoPlayer descifra y reproduce
 */
@UnstableApi
object DrmManager {

    private const val TAG = "DrmManager"

    /** UUID estándar de Widevine: EDEF8BA9-79D6-4ACE-A3C8-27DCD51D21ED */
    val WIDEVINE_UUID: UUID = UUID(-1301668207276963122L, -6645017420763422227L)

    /**
     * Crea un DrmSessionManager para reproducir contenido protegido con Widevine.
     *
     * @param licenseUrl URL del servidor de licencias (viene del backend en el
     *                   campo `payCoreAddress` o similar del LoginResultData)
     * @param token Token de autenticación (se envía como header al license server)
     * @param keySetId (Opcional) KeySetId persistente para offline playback
     *
     * @return DrmSessionManager listo para enchufar a un ExoPlayer
     */
    fun createWidevineSessionManager(
        licenseUrl: String,
        token: String,
        keySetId: ByteArray? = null,
        forceSessionsLicense: Boolean = false,
        multiSession: Boolean = false
    ): DrmSessionManager {
        require(licenseUrl.isNotBlank()) {
            "licenseUrl no puede ser vacía — el backend debe proveerla"
        }

        // 1. HttpMediaDrmCallback — hace la HTTP request al license server
        val dataSourceFactory = LicenseHttpDataSourceFactory(token)
        val mediaDrmCallback = HttpMediaDrmCallback(licenseUrl, dataSourceFactory)

        // 2. FrameworkMediaDrm — wrapper sobre android.media.MediaDrm
        val frameworkMediaDrm = FrameworkMediaDrm(WIDEVINE_UUID)

        // 3. DefaultDrmSessionManager.Builder — combina todo
        return DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(WIDEVINE_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
            .setKeyRequestParameters(emptyMap())  // TODO: completar con parámetros específicos
            .setMultiSession(multiSession)
            .setPlayClearSamplesWithoutKeys(false)  // No reproducir sin keys (anti-piracy)
            .build(mediaDrmCallback)
    }

    /**
     * Devuelve el Widevine security level del dispositivo.
     *
     * - L1: Hardware-backed (TPM). Máxima seguridad. Típico en TV Box certificados.
     * - L3: Software-backed. Menos seguro. Típico en móviles y TV Box chinos.
     *
     * El backend puede rechazar L3 para contenido premium.
     */
    fun getSecurityLevel(): String {
        return try {
            val mediaDrm = FrameworkMediaDrm(WIDEVINE_UUID)
            val level = mediaDrm.getPropertyString("securityLevel")
            mediaDrm.release()
            level.ifEmpty { "UNKNOWN" }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo obtener security level", e)
            "UNKNOWN"
        }
    }

    /**
     * Devuelve el Widevine version string (ej. "1.4.24.61").
     */
    fun getWidevineVersion(): String {
        return try {
            val mediaDrm = FrameworkMediaDrm(WIDEVINE_UUID)
            val version = mediaDrm.getPropertyString("version")
            mediaDrm.release()
            version
        } catch (e: Exception) {
            "unknown"
        }
    }

    /**
     * Devuelve el system ID de Widevine (fijo para L1: 0xEDEF8BA9...).
     */
    fun getSystemId(): String = WIDEVINE_UUID.toString().uppercase()
}

/**
 * HttpDataSource.Factory que inyecta headers de autenticación
 * en cada request al license server.
 *
 * El license server de Xuper valida:
 *  - **Authorization** — token de sesión del usuario
 *  - **X-Device-Id** — identificador del dispositivo
 *  - **X-Widevine-Id** — device unique ID (para asociar licencia a dispositivo)
 *  - **Content-Type** — siempre `application/octet-stream` (binary DRM request)
 */
@UnstableApi
private class LicenseHttpDataSourceFactory(
    private val token: String
) : HttpDataSource.Factory {

    override fun createDataSource(): HttpDataSource {
        // Usar OkHttp para tener control total de los headers
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .build()

        return OkHttpDataSourceForLicense(okHttpClient, token)
    }
}

/**
 * Wrapper simple sobre OkHttp para requests de licencia DRM.
 * Inyecta headers de auth en cada POST al license server.
 */
@UnstableApi
private class OkHttpDataSourceForLicense(
    private val client: okhttp3.OkHttpClient,
    private val token: String
) : HttpDataSource {

    private var currentRequest: okhttp3.Request? = null
    private var currentResponse: okhttp3.Response? = null
    private var currentBytesRead: Long = 0
    private var totalBytes: Long = 0

    override fun open(dataSpec: androidx.media3.datasource.DataSpec): Long {
        val builder = okhttp3.Request.Builder()
            .url(dataSpec.uri.toString())
            .post(okhttp3.RequestBody.create(
                okhttp3.MediaType.get("application/octet-stream"),
                dataSpec.data ?: ByteArray(0)
            ))
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/octet-stream")
            .addHeader("X-Device-Id", com.xuper.netxxus.data.auth.DeviceManager.customDeviceId)
            .addHeader("X-Widevine-Id", com.xuper.netxxus.data.auth.DeviceManager.widevineDeviceId)
            .addHeader("User-Agent", DrmManager.USER_AGENT)

        currentRequest = builder.build()
        currentResponse = client.newCall(currentRequest!!).execute()
        currentBytesRead = 0
        totalBytes = currentResponse?.body()?.contentLength() ?: 0
        return totalBytes
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val body = currentResponse?.body() ?: return -1
        val source = body.source()
        val read = source.read(
            okio.Buffer().write(buffer),
            length.toLong()
        ).toInt()
        if (read == -1) return -1
        currentBytesRead += read
        return read
    }

    override fun close() {
        currentResponse?.close()
        currentRequest = null
        currentResponse = null
    }

    override fun getUri(): android.net.Uri? = currentResponse?.request()?.url()?.let {
        android.net.Uri.parse(it.toString())
    }

    override fun setResponseContentType(contentType: String) {}

    override fun setRequestProperty(name: String, value: String) {
        // No-op: headers se inyectan en open()
    }

    override fun clearRequestProperty(name: String) {}

    override fun clearAllRequestProperties() {}

    override fun getResponseHeaders(): Map<String, List<String>> {
        return currentResponse?.headers()?.toMultimap() ?: emptyMap()
    }

    override fun getResponseCode(): Int = currentResponse?.code() ?: -1
}

// Constante para User-Agent
private const val USER_AGENT = "XuperHydra/4.35.0 (Widevine DRM)"
