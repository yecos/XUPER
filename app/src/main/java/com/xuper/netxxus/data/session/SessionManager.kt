package com.xuper.netxxus.data.session

import android.content.Context
import android.content.SharedPreferences
import com.xuper.netxxus.XuperApp
import com.xuper.netxxus.data.auth.DeviceManager
import com.xuper.netxxus.data.model.LoginResponse

/**
 * Gestiona la sesión del usuario — persistencia del token, userId, área, etc.
 *
 * Réplica de `cc.k` (singleton con campos estáticos) y `qb.z0` (persistencia)
 * de la APK original, pero adaptado a Kotlin con SharedPreferences en vez de
 * campos estáticos (más seguro y testeable).
 *
 * Campos persistidos:
 *  - accountType, username, password (¡HASHEADO!), areaCode
 *  - verificationToken, verificationCode
 *  - userId, token, userToken (del LoginResponse)
 *  - portalCodeList (lista de códigos de portal asignados)
 *  - expireDate, membershipLevel
 *
 * ⚠️ La password se guarda porque la app original la usa para auto-login.
 * En Android Keystore sería lo ideal, pero para mantener compatibilidad con
 * el backend original (que espera la password en cada heartbeat), la dejamos
 * en SharedPreferences cifrada con AES-GCM usando una key derivada del
 * androidId (no es ideal pero es lo que hace la app original).
 */
object SessionManager {

    private const val PREFS = "xuper_session"

    // Keys de SharedPreferences
    private const val KEY_ACCOUNT_TYPE = "account_type"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD_ENC = "password_enc"
    private const val KEY_AREA_CODE = "area_code"
    private const val KEY_VERIF_TOKEN = "verification_token"
    private const val KEY_VERIF_CODE = "verification_code"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_TOKEN = "user_token"
    private const val KEY_PORTAL_CODES = "portal_codes"  // JSON array
    private const val KEY_EXPIRE_DATE = "expire_date"
    private const val KEY_MEMBERSHIP = "membership_level"
    private const val KEY_EMAIL = "email"
    private const val KEY_CUSTOMER = "customer"
    private const val KEY_HEARTBEAT_TIME = "heartbeat_time"
    private const val KEY_LOGIN_TIME = "login_time"

    private val prefs: SharedPreferences by lazy {
        XuperApp.instance.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    // ========================================================================
    // Setters (llamados por AuthManager después del login)
    // ========================================================================

    /**
     * Guarda las credenciales de login (para auto-login posterior).
     * La password se guarda cifrada.
     */
    fun saveCredentials(
        accountType: String,
        username: String,
        password: String,
        areaCode: String = "",
        verificationToken: String = "",
        verificationCode: String = ""
    ) {
        prefs.edit().apply {
            putString(KEY_ACCOUNT_TYPE, accountType)
            putString(KEY_USERNAME, username)
            putString(KEY_PASSWORD_ENC, encryptPassword(password))
            putString(KEY_AREA_CODE, areaCode)
            putString(KEY_VERIF_TOKEN, verificationToken)
            putString(KEY_VERIF_CODE, verificationCode)
            putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
        }.apply()
    }

    /**
     * Actualiza los datos de sesión tras un login exitoso.
     * Replica de `ac.m1.a.onNext(LoginResult)`.
     */
    fun saveLoginResponse(response: LoginResponse) {
        prefs.edit().apply {
            response.userId?.let { putString(KEY_USER_ID, it) }
            response.userToken?.let { putString(KEY_USER_TOKEN, it) }
            response.membershipLevel?.let { putInt(KEY_MEMBERSHIP, it) }
            response.expireDate?.let { putString(KEY_EXPIRE_DATE, it) }
            // Token de sesión (el que va en headers Authorization)
            response.portalCodeList?.firstOrNull()?.let {
                putString(KEY_TOKEN, it.portalCode)
            }
            // Guardar lista de portal codes como JSON simple (comma-separated)
            response.portalCodeList?.joinToString(",") { it.portalCode }?.let {
                putString(KEY_PORTAL_CODES, it)
            }
        }.apply()
    }

    // ========================================================================
    // Getters (usados por AuthInterceptor y la UI)
    // ========================================================================

    val isLoggedIn: Boolean
        get() = prefs.getString(KEY_TOKEN, null) != null &&
                prefs.getString(KEY_USER_ID, null) != null

    val userId: String
        get() = prefs.getString(KEY_USER_ID, "") ?: ""

    val username: String
        get() = prefs.getString(KEY_USERNAME, "") ?: ""

    val token: String
        get() = prefs.getString(KEY_TOKEN, "") ?: ""

    val userToken: String
        get() = prefs.getString(KEY_USER_TOKEN, "") ?: ""

    val areaCode: String
        get() = prefs.getString(KEY_AREA_CODE, "") ?: ""

    val membershipLevel: Int
        get() = prefs.getInt(KEY_MEMBERSHIP, 0)

    val expireDate: String
        get() = prefs.getString(KEY_EXPIRE_DATE, "") ?: ""

    val portalCodes: List<String>
        get() = prefs.getString(KEY_PORTAL_CODES, "")
            ?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

    val email: String
        get() = prefs.getString(KEY_EMAIL, "") ?: ""

    val customer: String
        get() = prefs.getString(KEY_CUSTOMER, "") ?: ""

    val heartbeatIntervalSec: Long
        get() = prefs.getString(KEY_HEARTBEAT_TIME, null)?.toLongOrNull() ?: 300L

    // ========================================================================
    // Auto-login
    // ========================================================================

    /**
     * ¿Tiene credenciales guardadas para auto-login?
     */
    val canAutoLogin: Boolean
        get() = prefs.getString(KEY_USERNAME, null) != null &&
                prefs.getString(KEY_PASSWORD_ENC, null) != null

    /**
     * Recupera las credenciales para auto-login.
     * Returns null si no hay credenciales guardadas.
     */
    fun getCredentialsForAutoLogin(): AutoLoginCredentials? {
        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        val encPass = prefs.getString(KEY_PASSWORD_ENC, null) ?: return null
        val password = decryptPassword(encPass)
        return AutoLoginCredentials(
            accountType = prefs.getString(KEY_ACCOUNT_TYPE, "1") ?: "1",
            username = username,
            password = password,
            areaCode = prefs.getString(KEY_AREA_CODE, "") ?: "",
            verificationToken = prefs.getString(KEY_VERIF_TOKEN, "") ?: "",
            verificationCode = prefs.getString(KEY_VERIF_CODE, "") ?: ""
        )
    }

    // ========================================================================
    // Logout
    // ========================================================================

    /**
     * Borra todos los datos de sesión.
     * No toca las credenciales de auto-login (prefiere logout suave).
     */
    fun clearSession() {
        prefs.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_TOKEN)
            remove(KEY_USER_TOKEN)
            remove(KEY_PORTAL_CODES)
            remove(KEY_EXPIRE_DATE)
            remove(KEY_MEMBERSHIP)
            remove(KEY_HEARTBEAT_TIME)
            remove(KEY_LOGIN_TIME)
        }.apply()
    }

    /**
     * Logout completo — borra TODO incluyendo credenciales.
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    // ========================================================================
    // Cifrado de password (simple pero mejor que plain text)
    // ========================================================================

    /**
     * Cifra la password con AES-GCM usando key derivada del androidId.
     * No es tan seguro como Android Keystore, pero evita que la password
     * esté en plain text en SharedPreferences.
     */
    private fun encryptPassword(password: String): String {
        if (password.isEmpty()) return ""
        return try {
            val salt = DeviceManager.androidId.toByteArray()
            val key = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(
                    javax.crypto.spec.PBEKeySpec(
                        "xuper-hydra-v4.35".toCharArray(),
                        salt,
                        10000,
                        256
                    )
                )
            val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
            val keySpec = javax.crypto.spec.SecretKeySpec(key.encoded, "AES")
            val iv = ByteArray(12).also { java.security.SecureRandom().nextBytes(it) }
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keySpec, javax.crypto.spec.GCMParameterSpec(128, iv))
            val encrypted = cipher.doFinal(password.toByteArray())
            // Formato: iv_base64 + ":" + ciphertext_base64
            android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP) + ":" +
                android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            // Fallback: Base64 simple (NO usar en producción)
            android.util.Base64.encodeToString(password.toByteArray(), android.util.Base64.NO_WRAP)
        }
    }

    private fun decryptPassword(encPassword: String): String {
        if (encPassword.isEmpty()) return ""
        return try {
            val parts = encPassword.split(":")
            if (parts.size != 2) {
                // Fallback legacy (Base64 simple)
                return String(android.util.Base64.decode(encPassword, android.util.Base64.DEFAULT))
            }
            val iv = android.util.Base64.decode(parts[0], android.util.Base64.NO_WRAP)
            val ciphertext = android.util.Base64.decode(parts[1], android.util.Base64.NO_WRAP)
            val salt = DeviceManager.androidId.toByteArray()
            val key = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(
                    javax.crypto.spec.PBEKeySpec(
                        "xuper-hydra-v4.35".toCharArray(),
                        salt,
                        10000,
                        256
                    )
                )
            val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                javax.crypto.Cipher.DECRYPT_MODE,
                javax.crypto.spec.SecretKeySpec(key.encoded, "AES"),
                javax.crypto.spec.GCMParameterSpec(128, iv)
            )
            String(cipher.doFinal(ciphertext))
        } catch (e: Exception) {
            ""
        }
    }
}

data class AutoLoginCredentials(
    val accountType: String,
    val username: String,
    val password: String,
    val areaCode: String,
    val verificationToken: String,
    val verificationCode: String
)
