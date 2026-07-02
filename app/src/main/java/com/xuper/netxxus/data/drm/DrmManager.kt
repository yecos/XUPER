package com.xuper.netxxus.data.drm

import android.media.MediaDrm
import android.util.Log
import java.util.UUID

/**
 * Utilidades DRM para Xuper Hydra.
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
 * La integración real con ExoPlayer se hace en `PlayerActivity.kt` usando
 * `MediaItem.DrmConfiguration.Builder(licenseUri)`. Esta clase solo provee
 * utilidades de inspección de Widevine.
 */
object DrmManager {

    private const val TAG = "DrmManager"

    /** UUID estándar de Widevine: EDEF8BA9-79D6-4ACE-A3C8-27DCD51D21ED */
    val WIDEVINE_UUID: UUID = UUID(-1301668207276963122L, -6645017420763422227L)

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
            val mediaDrm = MediaDrm(WIDEVINE_UUID)
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
            val mediaDrm = MediaDrm(WIDEVINE_UUID)
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

    /**
     * Indica si el dispositivo soporta Widevine (todos los Android 5+ deberían).
     */
    fun isWidevineSupported(): Boolean {
        return try {
            MediaDrm(WIDEVINE_UUID).use { true }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * User-Agent que se envía en cada request al license server.
     */
    const val USER_AGENT = "XuperHydra/4.35.0 (Widevine DRM)"
}

// MediaDrm.use() extension para auto-release (API 23+ ya lo soporta nativamente,
// pero lo dejamos para compatibilidad con API 21+).
private inline fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R {
    try {
        return block(this)
    } finally {
        try { this?.close() } catch (_: Exception) {}
    }
}

// MediaDrm no implementa AutoCloseable en todas las versiones, así que
// este wrapper lo maneja manualmente.
private inline fun <R> MediaDrm.use(block: (MediaDrm) -> R): R {
    try {
        return block(this)
    } finally {
        try { this.release() } catch (_: Exception) {}
    }
}
