package com.xuper.netxxus.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz Retrofit para la API de Xuper Hydra.
 *
 * ⚠️ ESTADO: ESQUELETO — los endpoints reales se completan después de capturar
 * el tráfico con mitmproxy (ver docs/CAPTURA-TRAFICO.md).
 *
 * La app original usa los siguientes dominios (cifrados en strings.xml):
 *  - portal_main / portal_backup   → autenticación, datos del usuario
 *  - epg_main / epg_backup         → guía de canales (EPG)
 *  - dccore_main / dccore_backup   → endpoint inicial de bootstrap
 *  - ad_main / ad_backup           → anuncios
 *  - notice_main / notice_backup   → notificaciones push
 *  - upgrade_main / upgrade_backup → check de versiones
 *  - bigbee_main / bigbee_backup   → (uso interno)
 *
 * Cuando tengas el reporte de mitmproxy (docs/ENDPOINTS-CAPTURADOS.md),
 * completa cada función con su path + query params + body real.
 */
interface XuperApi {

    // ========================================================================
    // Bootstrap — primera llamada al iniciar la app (servidor DCS)
    // ========================================================================

    /**
     * Obtiene la lista de URLs reales del portal/EPG/ads/etc.
     * Equivalente a v2.a.f23166a.n() en la APK original.
     *
     * TODO: reemplazar URL con la capturada en mitmproxy
     */
    @GET
    suspend fun fetchDcsConfig(@Url url: String): Response<ResponseBody>

    // ========================================================================
    // Autenticación
    // ========================================================================

    /**
     * Login con cuenta + password.
     * Equivalente a ac/a.java / ac/m1.java en la APK original.
     *
     * TODO: completar path + body según captura
     */
    @POST("TODO_login_path")
    @FormUrlEncoded
    suspend fun login(
        @Field("account") account: String,
        @Field("password") password: String,
        @Field("device_id") deviceId: String,
        @Field("portal_code") portalCode: String
    ): Response<ResponseBody>

    /**
     * Login con código QR (escaneado desde móvil).
     */
    @POST("TODO_qr_login_path")
    @FormUrlEncoded
    suspend fun loginWithQrCode(
        @Field("qr_token") qrToken: String,
        @Field("device_id") deviceId: String
    ): Response<ResponseBody>

    /**
     * Cierra la sesión activa.
     */
    @POST("TODO_logout_path")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    /**
     * Información del usuario (membresía, expiración).
     */
    @GET("TODO_user_info_path")
    suspend fun getUserInfo(
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    // ========================================================================
    // Home
    // ========================================================================

    /**
     * Carga las categorías del home (Live, Movies, Series, etc.).
     */
    @GET("TODO_home_categories_path")
    suspend fun fetchHomeCategories(
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    /**
     * Carga los carruseles del home (Novedades, Populares, etc.).
     */
    @GET("TODO_home_carousels_path")
    suspend fun fetchHomeCarousels(
        @Header("Authorization") token: String,
        @Query("category") category: String? = null
    ): Response<ResponseBody>

    // ========================================================================
    // VOD (Películas y Series)
    // ========================================================================

    /**
     * Lista de VOD por categoría.
     */
    @GET("TODO_vod_list_path")
    suspend fun fetchVodList(
        @Header("Authorization") token: String,
        @Query("category_id") categoryId: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<ResponseBody>

    /**
     * Detalle de una película/serie.
     */
    @GET("TODO_vod_detail_path")
    suspend fun fetchVodDetail(
        @Header("Authorization") token: String,
        @Path("vod_id") vodId: String
    ): Response<ResponseBody>

    /**
     * Episodios de una serie.
     */
    @GET("TODO_vod_episodes_path")
    suspend fun fetchVodEpisodes(
        @Header("Authorization") token: String,
        @Path("vod_id") vodId: String,
        @Query("season") season: Int? = null
    ): Response<ResponseBody>

    /**
     * Búsqueda de VOD.
     */
    @GET("TODO_vod_search_path")
    suspend fun searchVod(
        @Header("Authorization") token: String,
        @Query("q") query: String,
        @Query("type") type: String? = null  // "movie" | "series" | "all"
    ): Response<ResponseBody>

    // ========================================================================
    // Live TV
    // ========================================================================

    /**
     * Lista de categorías de canales en vivo.
     */
    @GET("TODO_live_categories_path")
    suspend fun fetchLiveCategories(
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    /**
     * Lista de canales en vivo por categoría.
     */
    @GET("TODO_live_channels_path")
    suspend fun fetchLiveChannels(
        @Header("Authorization") token: String,
        @Query("category_id") categoryId: String? = null
    ): Response<ResponseBody>

    /**
     * URL del stream de un canal en vivo (HLS / MPEG-DASH).
     */
    @GET("TODO_live_stream_path")
    suspend fun fetchLiveStreamUrl(
        @Header("Authorization") token: String,
        @Path("channel_id") channelId: String
    ): Response<ResponseBody>

    /**
     * EPG (guía de programas) para los canales en vivo.
     */
    @GET("TODO_epg_path")
    suspend fun fetchEpg(
        @Header("Authorization") token: String,
        @Query("channel_id") channelId: String? = null,
        @Query("date") date: String? = null  // YYYY-MM-DD
    ): Response<ResponseBody>

    // ========================================================================
    // Mi cuenta
    // ========================================================================

    /**
     * Historial de pedidos/renovaciones.
     */
    @GET("TODO_order_history_path")
    suspend fun fetchOrderHistory(
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    /**
     * Renovar membresía.
     */
    @POST("TODO_renew_path")
    @FormUrlEncoded
    suspend fun renewMembership(
        @Header("Authorization") token: String,
        @Field("plan_id") planId: String
    ): Response<ResponseBody>

    // ========================================================================
    // Misc
    // ========================================================================

    /**
     * Lista de servidores de actualización (para self-update).
     */
    @GET("TODO_upgrade_check_path")
    suspend fun checkAppUpgrade(
        @Query("version_code") versionCode: Int
    ): Response<ResponseBody>

    /**
     * Notificaciones push / avisos del portal.
     */
    @GET("TODO_notice_path")
    suspend fun fetchNotices(
        @Header("Authorization") token: String
    ): Response<ResponseBody>
}
