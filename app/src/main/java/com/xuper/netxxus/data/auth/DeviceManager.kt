package com.xuper.netxxus.data.auth

import android.content.Context
import android.media.MediaDrm
import android.os.Build
import android.provider.Settings
import android.util.Base64
import com.xuper.netxxus.XuperApp
import java.net.NetworkInterface
import java.util.UUID

/**
 * Genera y cachea identificadores únicos del dispositivo para enviar al backend.
 *
 * Réplica de `cc.c0` de la APK original. Campos generados:
 *  - **androidId** — `Settings.Secure.ANDROID_ID` (único por instalación)
 *  - **macAddress** — MAC de la interfaz de red (formato `02:00:00:00:00:00` en Android 6+)
 *  - **widevineDeviceId** — `MediaDrm.getPropertyByteArray("deviceUniqueId")` en Base64
 *  - **sn** — "Synthetic SN" basado en androidId + widevine (10 hex chars)
 *  - **customDeviceId** — UUID generado una sola vez y persistido en SharedPreferences
 *
 * El backend usa estos IDs para:
 *  - Identificar el dispositivo en el login
 *  - Validar región (`account_area_invalid`)
 *  - Asociar suscripciones y limiter de dispositivos
 *  - Generar la DRM license request (Widevine device ID)
 */
object DeviceManager {

    private const val PREFS = "xuper_device"
    private const val KEY_CUSTOM_ID = "custom_device_id"
    private const val KEY_WV_DEVICE_ID = "widevine_device_id_b64"

    /** UUID estándar de Widevine: EDEF8BA9-79D6-4ACE-A3C8-27DCD51D21ED */
    private val WIDEVINE_UUID = UUID(-1301668207276963122L, -6645017420763422227L)

    /** Android ID — único por instalación (resetea en factory reset) */
    val androidId: String by lazy {
        try {
            Settings.Secure.getString(
                XuperApp.instance.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /** MAC address de la interfaz de red primaria (formato colon-separated) */
    val macAddress: String by lazy {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()?.toList() ?: emptyList()
            for (iface in interfaces) {
                if (iface.isLoopback) continue
                val mac = iface.hardwareAddress ?: continue
                if (mac.any { it != 0.toByte() }) {
                    return@lazy mac.joinToString(":") { String.format("%02X", it) }
                }
            }
            "02:00:00:00:00:00"
        } catch (e: Exception) {
            "02:00:00:00:00:00"
        }
    }

    /**
     * Widevine Device Unique ID en Base64.
     * Replica de `cc.c0.s()` de la APK original.
     */
    val widevineDeviceId: String by lazy {
        val prefs = XuperApp.instance.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getString(KEY_WV_DEVICE_ID, null)?.let { return@lazy it }

        try {
            val mediaDrm = MediaDrm(WIDEVINE_UUID)
            val deviceBytes = mediaDrm.getPropertyByteArray("deviceUniqueId")
            mediaDrm.release()
            val b64 = if (deviceBytes != null) {
                Base64.encodeToString(deviceBytes, Base64.NO_WRAP)
            } else ""
            prefs.edit().putString(KEY_WV_DEVICE_ID, b64).apply()
            b64
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Synthetic Serial Number — 12 hex chars basado en androidId + Widevine ID.
     * Replica de `k2.j.j()` en la APK original.
     */
    val sn: String by lazy {
        val raw = (androidId + widevineDeviceId).hashCode().toString(16).padStart(8, '0')
        "SN${raw.take(8).uppercase()}"
    }

    /**
     * UUID persistente (custom). Generado una sola vez.
     */
    val customDeviceId: String by lazy {
        val prefs = XuperApp.instance.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getString(KEY_CUSTOM_ID, null) ?: run {
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_CUSTOM_ID, newId).apply()
            newId
        }
    }

    val deviceModel: String by lazy { Build.MODEL }
    val deviceManufacturer: String by lazy { Build.MANUFACTURER }
    val androidVersion: String by lazy { Build.VERSION.RELEASE }
    val sdkInt: Int = Build.VERSION.SDK_INT

    /**
     * Resumen completo del dispositivo para enviar en el login.
     */
    fun toDeviceInfoMap(): Map<String, String> = mapOf(
        "device_id" to customDeviceId,
        "android_id" to androidId,
        "mac" to macAddress,
        "sn" to sn,
        "widevine_id" to widevineDeviceId,
        "model" to deviceModel,
        "manufacturer" to deviceManufacturer,
        "android_version" to androidVersion,
        "sdk" to sdkInt.toString(),
        "app_version" to "4.35.0-redesign"
    )
}
