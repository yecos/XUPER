# Análisis técnico — Xuper Hydra APK original

> Generado automáticamente a partir del análisis de `xuper.apk` (35 MB, build del 2026-06-07).

## Identificación

| Atributo | Valor |
|---|---|
| Nombre | Xuper Hydra |
| Package | `com.xuper.netxxus` |
| Versión | 4.34.4 (versionCode 43404) |
| Firma | Self-signed `CN=Netxxus` (válido 2026→2051) |
| minSdk | 19 (Android 4.4) |
| targetSdk | 33 (Android 13) |
| compileSdk | 33 |
| ABI | `arm64-v8a`, `armeabi-v7a` (sin x86) |
| Multidex | 3 archivos `classes.dex`, 5167 clases |

## Tipo de app

Aplicación de streaming **IPTV/VOD** (TV en vivo + películas/series) orientada a:
- Android TV / TV Box / Fire TV Stick (landscape forzado, design 1920×1080)
- Leanback launcher (`android.intent.category.LEANBACK_LAUNCHER`)
- También soporta móviles Android (`purchase_magis = Xuper (mobile)`)

## Stack técnico

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java puro (sin Kotlin en código de la app) |
| Build | Gradle (APK firmada con `apksigner` v1+v2+v3) |
| UI base | `Theme.AppCompat.Light.NoActionBar` (Material 2, **no Material 3**) |
| Reproctor | IJKPlayer + ExoPlayer (`tv.danmaku.ijk.*`) |
| Imágenes | Glide + OkHttp3 integration |
| Push | Firebase Messaging + Umeng Push (Taobao Accs) |
| Crash | Firebase Crashlytics (incl. NDK) |
| Analytics | Firebase Analytics + Google Measurement |
| Navegación | TheRouter (`com.therouter.*`) |
| BD | DBFlow (SQLite ORM) + Qiniu DNS |
| Multidex | Bytedance boost_multidex |
| Reactive | RxJava 2 |

## Estructura de paquetes

```
com/
├── interactive/brasiliptv/      ← App base (AppWrapper, WelcomeActivity)
├── main/ui/activity/            ← HomeActivity (2892 líneas)
├── vod/ui/activity/             ← Detalle VOD, búsqueda, categorías
├── live/ui/activity/            ← TV en vivo, match schedules, voice search
├── mine/ui/activity/            ← UserCenter, OrderHistory, InviteFriends
├── login/ui/activity/           ← ForcePasswordChange
├── download/activity/           ← DownloadActivity, LocalPlayActivity
├── module/ui/activity/          ← WebActivity, CommonWebActivity
├── adimage/view/                ← LaunchAdView (banner de arranque)
├── advertlib/                   ← Lib de anuncios
└── core/sysopt/mark/            ← Custom views (WelcomeRootView, etc.)
```

## Activities (33 total)

Las actividades principales son:
- `WelcomeActivity` — Splash + ad launch (launcher principal)
- `HomeActivity` — Home con sidebar + carruseles (2892 líneas)
- `VodDetailsActivity`, `VodCategoryActivity`, `VodSearchActivity`
- `LiveFreeActivity`, `MatchScheduleActivity`, `MatchDetailActivity`
- `UserCenterActivity`, `OrderHistoryActivity`, `InviteFriendsActivity`
- `LoginActivity`, `ForcePasswordChangeActivity`
- `LocalPlayActivity` — Reproductor local
- `FilterActivity`, `TopicActivity`, `ActorDetailsActivity`
- `KidsCategoryActivity` — Modo niños
- `DisplayQRCodeActivity`, `EventCenterActivity`

## Recursos

| Tipo | Cantidad | Notas |
|---|---|---|
| Layouts XML | 518 | Mayoría AppCompat legacy |
| Strings EN | 1513 | Idioma default en `values/` |
| Strings ES | 1303 (220 faltantes) | Traducción parcial |
| Strings PT | 206 | Traducción mínima |
| Colores | 562 | Solo 1 marca: `color_main=#c91923` |
| Styles | 728 | Mayoría heredados de AppCompat |
| Drawables | 20 carpetas | Incl. `drawable-es/`, `drawable-pt-xhdpi/` |
| Assets | 5 archivos | `amazingkids.otf`, `domain_test.json`, etc. |

## Idiomas detectados

```
values/           ← EN (default, 1513 strings)
values-es/        ← Español (1303 strings, 220 faltantes)
values-pt/        ← Portugués (206 strings)
values-pt-rBR/    ← Portugués Brasil (47 strings)
values-en-rUS/    ← English US (29 strings, override)
values-en-rGB/    ← English UK (47 strings)
values-en-rCA/    ← English CA
values-en-rAU/    ← English AU
values-en-rIN/    ← English IN
values-en-rXC/    ← Pseudo-loc (testing)
+ 60+ otros locales
```

## Permisos (AndroidManifest)

```xml
INTERNET, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE,
READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE,
READ_MEDIA_AUDIO, WAKE_LOCK, GET_TASKS,
REQUEST_INSTALL_PACKAGES, POST_NOTIFICATIONS,
AD_ID (Google), RECEIVE (C2DM Firebase),
BIND_GET_INSTALL_REFERRER_SERVICE,
MOUNT_UNMOUNT_FILESYSTEMS,
CHANGE_BADGE (Huawei), BADGE_ICON (Vivo)
```

## Branding visual (original)

| Color | Hex | Uso |
|---|---|---|
| `color_main` | `#c91923` | Rojo apagado (marca) |
| `color_0b0b0b` | `#0b0b0b` | Fondo principal |
| `color_1a1a1a` | `#1a1a1a` | Fondo secundario |
| `color_ff3333` | `#ff3333` | Acento (notificaciones) |
| `color_app_name` | `#ffffff` | Texto app name |
| `color_b3b3b3` | `#b3b3b3` | Texto secundario |

## Problemas detectados

1. **Incoherencia de tema**: `BaseAppTheme` extiende `Theme.AppCompat.Light.NoActionBar` (tema claro) pero las pantallas usan fondos oscuros → parpadeo blanco al iniciar Activities.
2. **220 strings sin traducir al ES**: incluyendo `app_name`, `home_category_*`, `error_code_*`, `scan_qr*`, `settings`.
3. **Sin Material 3**: usa AppCompat legacy, sin dark mode formal, sin componentes modernos.
4. **Layouts fijos en píxeles**: `1920×1080` hardcodeado en meta-data → se ve mal en tablets/TVs 4K reales.
5. **Sin `values-night/`**: no hay dark mode formal (aunque la app ya es oscura por diseño).
6. **Icono `ic_launcher` solo en webp**: sin adaptive icon (Android 8+).
7. **Cleartext traffic permitido**: `network_security_config.xml` permite HTTP sin restricciones.
8. **`domain_test.json` con placeholders**: todos los endpoints son `"xx"` (no configurados).

## Firma

```
Owner: CN=Netxxus, OU=Netxxus, O=Netxxus, L=Netxxus, ST=Netxxus, C=Netxxus
SHA1:   89:9F:12:91:8B:18:7A:9C:77:32:FF:5C:D7:0D:F8:0A:F0:C5:3A:9B
SHA256: DE:4D:40:19:7B:71:DB:B3:0D:76:32:C5:C0:E6:BC:E3:03:3B:2C:AA:56:89:99:A4:14:32:1E:8B:A5:40:01:BF
Algoritmo: SHA256withRSA, 2048-bit
Válido: 2026-05-21 → 2051-05-15
```

Al reempaquetar la APK con `apktool b` + `apksigner`, la firma cambia → hay que desinstalar la app oficial antes de instalar la modificada.
