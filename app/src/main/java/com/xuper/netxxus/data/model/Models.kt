package com.xuper.netxxus.data.model

/**
 * Modelos de datos de Xuper Hydra.
 *
 * ⚠️ ESTADO: ESQUELETO. Los campos reales se completan después de capturar
 * las responses con mitmproxy (ver docs/CAPTURA-TRAFICO.md).
 *
 * La app original usa Gson para parsear JSON. Los nombres de campo en esta
 * clase se actualizan cuando veas el formato real del backend.
 */

// ========================================================================
// Auth
// ========================================================================

data class LoginRequest(
    val account: String,
    val password: String,
    val deviceId: String,
    val portalCode: String
)

data class LoginResponse(
    val userId: String?,
    val userToken: String?,
    val portalCodeList: List<PortalCode>?,
    val expireDate: String?,
    val membershipLevel: Int?
)

data class PortalCode(
    val portalCode: String,
    val area: String?
)

// ========================================================================
// Home
// ========================================================================

data class HomeCategory(
    val id: String,
    val name: String,
    val type: String,  // "live" | "vod" | "topic"
    val icon: String?
)

data class HomeCarousel(
    val title: String,
    val items: List<ContentItem>
)

// ========================================================================
// VOD (Películas y Series)
// ========================================================================

data class ContentItem(
    val id: String,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val type: String,  // "movie" | "series" | "anime"
    val year: Int?,
    val rating: Double?,
    val duration: String?,
    val genres: List<String>?
)

data class VodDetail(
    val id: String,
    val title: String,
    val synopsis: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val year: Int?,
    val rating: Double?,
    val duration: String?,
    val genres: List<String>?,
    val cast: List<String>?,
    val director: String?,
    val episodes: List<Episode>?,
    val related: List<ContentItem>?
)

data class Episode(
    val id: String,
    val number: Int,
    val season: Int,
    val title: String,
    val duration: String?,
    val thumbnailUrl: String?,
    val streamUrl: String?
)

// ========================================================================
// Live TV
// ========================================================================

data class LiveCategory(
    val id: String,
    val name: String,
    val icon: String?
)

data class LiveChannel(
    val id: String,
    val name: String,
    val logo: String?,
    val categoryId: String,
    val streamUrl: String?,
    val epgCurrent: Program?,
    val epgNext: Program?
)

data class Program(
    val title: String,
    val startTime: String,  // ISO 8601
    val endTime: String,
    val description: String?
)

// ========================================================================
// Mi cuenta
// ========================================================================

data class UserInfo(
    val userId: String,
    val username: String,
    val email: String?,
    val membershipLevel: Int,
    val expireDate: String,
    val avatar: String?
)

data class Order(
    val id: String,
    val date: String,
    val amount: Double,
    val currency: String,
    val planName: String,
    val status: String
)

// ========================================================================
// Bootstrap (DCS)
// ========================================================================

data class DcsConfig(
    val portal: List<BusinessEntry>,
    val epg: List<BusinessEntry>,
    val ads: List<BusinessEntry>,
    val notice: List<BusinessEntry>,
    val upgrade: List<BusinessEntry>
)

data class BusinessEntry(
    val businessEntry: String,  // "portal" | "epg" | "ads" | ...
    val details: String         // URL(s) separadas por "|"
)
