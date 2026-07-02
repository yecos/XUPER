package com.xuper.netxxus.data.session

import com.xuper.netxxus.data.auth.DeviceManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor OkHttp que inyecta automáticamente:
 *  - **Authorization** header con el token de sesión
 *  - **X-Device-Id** header con el custom device ID
 *  - **X-SN** header con el serial number
 *  - **X-Widevine-Id** header con el Widevine device ID (Base64)
 *  - **X-App-Version** header
 *  - **User-Agent** custom (para que el backend identifique la app)
 *
 * Réplica del comportamiento de `pb.c.f21020b.a()` + `pb.d` de la APK original
 * que añade estos headers en cada request HTTP.
 *
 * Si la respuesta es 401, opcionalmente puede disparar un re-login (TODO).
 */
class SessionInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        // Authorization header (solo si hay sesión activa)
        if (SessionManager.isLoggedIn) {
            builder.addHeader("Authorization", "Bearer ${SessionManager.token}")
            builder.addHeader("X-User-Id", SessionManager.userId)
            builder.addHeader("X-User-Token", SessionManager.userToken)
        }

        // Device headers (siempre presentes)
        DeviceManager.toDeviceInfoMap().forEach { (key, value) ->
            if (value.isNotEmpty()) {
                // Convertir snake_case → X-Header-Case
                val headerName = "X-" + key.split("_").joinToString("-") { it.capitalize() }
                builder.addHeader(headerName, value)
            }
        }

        // User-Agent custom (para que el backend identifique la app)
        builder.header("User-Agent", USER_AGENT)

        // Portal code header (algunos endpoints lo requieren)
        if (SessionManager.portalCodes.isNotEmpty()) {
            builder.addHeader("X-Portal-Code", SessionManager.portalCodes.joinToString(","))
        }

        val request = builder.build()
        val response = chain.proceed(request)

        // Si 401, la sesión caducó — limpiar para forzar re-login
        if (response.code == 401 && SessionManager.isLoggedIn) {
            SessionManager.clearSession()
            // TODO: disparar intent a LoginActivity
        }

        return response
    }

    companion object {
        const val USER_AGENT = "XuperHydra/4.35.0 (Android TV; ${DeviceManager.deviceManufacturer} ${DeviceManager.deviceModel})"
    }
}
