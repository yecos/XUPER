package com.xuper.netxxus.data.auth

import android.util.Log
import com.xuper.netxxus.data.api.ApiClient
import com.xuper.netxxus.data.api.XuperApi
import com.xuper.netxxus.data.model.LoginResponse
import com.xuper.netxxus.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Maneja el flujo completo de autenticación con el backend de Xuper Hydra.
 *
 * Réplica de los flows de `ac.a`, `ac.m1`, `ac.g0`, `ac.t` de la APK original.
 *
 * Flujos implementados:
 *  1. **bootstrap()** — primera llamada al servidor DCS para obtener URLs reales
 *  2. **login(account, password)** — login normal con credenciales
 *  3. **autoLogin()** — intenta login con credenciales guardadas
 *  4. **loginWithQrToken(token)** — login escaneando QR desde el móvil
 *  5. **sendHeartbeat()** — keep-alive cada N segundos (default 5 min)
 *  6. **logout()** — invalida el token en el servidor y limpia la sesión local
 *
 * Códigos de retorno del backend (de la APK original):
 *  - `aaa100094` — login OK, se guardan token + userId
 *  - `db_ready_report` — login OK con portalCodeList
 *  - `account_incorrect_account_or_password` — credenciales inválidas
 *  - `account_forbidden` — cuenta bloqueada
 *  - `account_frozen` — cuenta congelada
 *  - `account_area_invalid` — región no soportada
 *  - `account_blacklist` — área en blacklist
 *  - `account_disenable` — dispositivo bloqueado
 *  - `account_device_not_support` — dispositivo no compatible
 *  - `account_already_login` — la cuenta ya está activa en otro dispositivo
 *
 * ⚠️ Los paths exactos se completan después de la captura con mitmproxy.
 */
class AuthManager(private val api: XuperApi = ApiClient.api) {

    /**
     * Etapa 1: Bootstrap — llama al servidor DCS para recibir URLs reales.
     * Replica de `WelcomeActivity.E6()` + `v2.a.f23166a.n(...)`.
     *
     * @return true si el bootstrap fue exitoso
     */
    suspend fun bootstrap(): Boolean = withContext(Dispatchers.IO) {
        if (ApiClient.DCS_BOOTSTRAP_URL.isBlank()) {
            Log.w(TAG, "DCS_BOOTSTRAP_URL no configurada — ejecuta captura mitmproxy primero")
            return@withContext false
        }
        try {
            val response = api.fetchDcsConfig(ApiClient.DCS_BOOTSTRAP_URL)
            if (response.isSuccessful) {
                // TODO: parsear respuesta y llenar ApiClient.portalBaseUrl / epgBaseUrl
                // El formato está en `LogResult` / `Business` / `URLInfo` del original
                Log.i(TAG, "Bootstrap OK")
                true
            } else {
                Log.e(TAG, "Bootstrap HTTP ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Bootstrap exception", e)
            false
        }
    }

    /**
     * Etapa 2: Login con cuenta y password.
     * Replica de `ac.a` (LoginActivity) → `ac.m1.onNext(LoginResult)`.
     *
     * @return AuthResult con el resultado del login
     */
    suspend fun login(
        account: String,
        password: String,
        areaCode: String = ""
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            val deviceId = DeviceManager.customDeviceId
            val portalCode = SessionManager.portalCodes.firstOrNull() ?: ""

            val response = api.login(
                account = account,
                password = password,
                deviceId = deviceId,
                portalCode = portalCode
            )

            handleLoginResponse(response, account, password, areaCode)
        } catch (e: Exception) {
            Log.e(TAG, "login exception", e)
            AuthResult.Error("network_error", e.message ?: "Error de red")
        }
    }

    /**
     * Etapa 2 (alt): Login con código QR.
     * El usuario escanea un QR mostrado en la TV desde la app móvil.
     */
    suspend fun loginWithQrToken(qrToken: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val response = api.loginWithQrCode(
                qrToken = qrToken,
                deviceId = DeviceManager.customDeviceId
            )
            handleLoginResponse(response, "", "", "")
        } catch (e: Exception) {
            AuthResult.Error("network_error", e.message ?: "Error de red")
        }
    }

    /**
     * Etapa 3: Auto-login usando credenciales guardadas.
     * Llamado al arrancar la app si el usuario tenía sesión.
     */
    suspend fun autoLogin(): AuthResult = withContext(Dispatchers.IO) {
        val creds = SessionManager.getCredentialsForAutoLogin()
            ?: return@withContext AuthResult.Error("no_credentials", "Sin credenciales guardadas")

        login(
            account = creds.username,
            password = creds.password,
            areaCode = creds.areaCode
        )
    }

    /**
     * Etapa 4: Heartbeat — keep-alive con el servidor.
     * Replica de `bc.z1.B2(HeartBeatBean)` de la APK original.
     *
     * Llamado cada `SessionManager.heartbeatIntervalSec` segundos por
     * `HeartbeatWorker` (WorkManager).
     *
     * @return true si el heartbeat fue OK, false si la sesión caducó
     */
    suspend fun sendHeartbeat(): Boolean = withContext(Dispatchers.IO) {
        if (!SessionManager.isLoggedIn) return@withContext false

        try {
            // TODO: llamar api.sendHeartbeat(token) cuando se complete la interfaz
            // Por ahora, solo verificar que la sesión siga activa localmente
            val expire = SessionManager.expireDate
            if (expire.isNotEmpty()) {
                // TODO: parsear expireDate y comparar con ahora
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Heartbeat failed", e)
            false
        }
    }

    /**
     * Etapa 5: Logout.
     * Invalida el token en el servidor y limpia la sesión local.
     */
    suspend fun logout(): Boolean = withContext(Dispatchers.IO) {
        if (!SessionManager.isLoggedIn) return@withContext true

        try {
            api.logout("Bearer ${SessionManager.token}")
            // No importar si falla el logout del server, limpiar local igual
        } catch (e: Exception) {
            Log.w(TAG, "Logout del servidor falló, limpiando local", e)
        }

        SessionManager.clearSession()
        true
    }

    // ========================================================================
    // Helper: procesar la respuesta de login
    // ========================================================================

    private fun handleLoginResponse(
        response: Response<okhttp3.ResponseBody>,
        account: String,
        password: String,
        areaCode: String
    ): AuthResult {
        if (!response.isSuccessful) {
            return AuthResult.Error(
                code = "http_${response.code()}",
                message = "Error HTTP ${response.code()}"
            )
        }

        val body = response.body()?.string() ?: return AuthResult.Error(
            code = "empty_body",
            message = "Respuesta vacía del servidor"
        )

        // TODO: cuando tengamos el formato real del JSON (vía mitmproxy),
        // reemplazar este parseo manual por Gson + LoginResponse data class.
        // Por ahora, intentamos extraer campos comunes.
        return try {
            val json = org.json.JSONObject(body)
            val returnCode = json.optString("returnCode", "")
            val errorMessage = json.optString("errorMessage", "")
            val data = json.optJSONObject("data")

            when (returnCode) {
                "aaa100094", "db_ready_report" -> {
                    // Login exitoso
                    val userId = data?.optString("userId") ?: ""
                    val token = data?.optString("token") ?: ""
                    val userToken = data?.optString("userToken") ?: ""
                    val expireDate = data?.optString("activeTime") ?: ""
                    val membershipLevel = data?.optInt("remainingDays", 0) ?: 0

                    // Guardar credenciales (para auto-login)
                    if (account.isNotEmpty() && password.isNotEmpty()) {
                        SessionManager.saveCredentials(
                            accountType = "1",
                            username = account,
                            password = password,
                            areaCode = areaCode
                        )
                    }

                    // Guardar datos de sesión
                    SessionManager.saveLoginResponse(
                        LoginResponse(
                            userId = userId,
                            userToken = userToken,
                            portalCodeList = emptyList(),
                            expireDate = expireDate,
                            membershipLevel = membershipLevel
                        )
                    )

                    AuthResult.Success(userId, token)
                }

                "account_incorrect_account_or_password" ->
                    AuthResult.Error(returnCode, "Usuario o contraseña incorrectos")
                "account_forbidden" ->
                    AuthResult.Error(returnCode, "Cuenta bloqueada. Contacta al distribuidor")
                "account_frozen" ->
                    AuthResult.Error(returnCode, "Cuenta congelada. Contacta al servicio al cliente")
                "account_area_invalid" ->
                    AuthResult.Error(returnCode, "Esta app no se puede usar en tu región")
                "account_blacklist" ->
                    AuthResult.Error(returnCode, "Tu área está en lista negra. Contacta al distribuidor")
                "account_disenable" ->
                    AuthResult.Error(returnCode, "Dispositivo bloqueado. Contacta al servicio al cliente")
                "account_device_not_support" ->
                    AuthResult.Error(returnCode, "Tu dispositivo no es compatible con esta app")
                "account_already_login" ->
                    AuthResult.Error(returnCode, "La cuenta ya está activa en otro dispositivo")
                else ->
                    AuthResult.Error(returnCode, errorMessage.ifEmpty { "Error desconocido: $returnCode" })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parseo de login falló", e)
            AuthResult.Error("parse_error", "No se pudo parsear la respuesta del servidor")
        }
    }

    companion object {
        private const val TAG = "AuthManager"
    }
}

/**
 * Resultado de una operación de autenticación.
 */
sealed class AuthResult {
    data class Success(val userId: String, val token: String) : AuthResult()
    data class Error(val code: String, val message: String) : AuthResult()
}
