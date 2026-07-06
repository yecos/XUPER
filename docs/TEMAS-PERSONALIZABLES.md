# 🎨 Guía de Temas Personalizables — XUPER HYDRA

## Resumen

El sistema de temas de XUPER HYDRA permite cambiar completamente la "cara" visual de la app (colores, tipografía, formas, layout, features) **sin tocar una sola línea de código Kotlin**. Solo editas un archivo JSON y compilas.

---

## 📁 Estructura de archivos

```
app/src/main/assets/
├── theme-config.json        ← Tema ACTIVO (se usa al compilar)
├── theme-red.json           ← Ejemplo: tema rojo (Netflix-style)
├── theme-blue.json          ← Ejemplo: tema azul
├── theme-green.json         ← Ejemplo: tema verde
├── theme-purple.json        ← Ejemplo: tema púrpura
├── theme-dark.json          ← Ejemplo: tema oscuro puro (blanco/negro)
└── theme-tu-marco.json      ← ¡TU TEMA PERSONALIZADO AQUÍ!
```

---

## 🚀 Cómo crear TU propio tema

### 1. Copia el tema base

```bash
cp app/src/main/assets/theme-config.json app/src/main/assets/theme-mi-marca.json
```

### 2. Edita el JSON

Abre `theme-mi-marca.json` y personaliza:

#### 🎨 Colores (`colors`)

```json
"colors": {
  "brandPrimary": "#TU_COLOR_HEX",       // Color principal de marca (botones, acentos)
  "brandPrimaryDark": "#OSCURO",         // Versión oscura (cards seleccionadas)
  "brandPrimaryLight": "#CLARO",         // Versión clara (hover, focus)
  "brandSecondary": "#SECUNDARIO",       // Color secundario
  "brandAccent": "#ACENTO",              // Acento especial (oro, etc.)

  "surface": "#FONDO_CARDS",             // Fondo de tarjetas
  "surfaceVariant": "#FONDO_CARDS_VAR",  // Variante
  "surfaceContainer": "#CONTENEDOR",     // Contenedores
  "surfaceContainerHigh": "#CONT_ALTO",
  "surfaceContainerHighest": "#CONT_MAX",

  "background": "#FONDO_PANTALLA",       // Fondo principal
  "backgroundSecondary": "#FONDO_SEC",

  "onPrimary": "#TEXTO_SOBRE_PRIMARIO",  // Texto sobre color primario
  "onSurface": "#TEXTO_SOBRE_SUPERFICIE",
  "onSurfaceVariant": "#TEXTO_SECUNDARIO",
  "onBackground": "#TEXTO_FONDO",
  "onError": "#TEXTO_ERROR",

  "error": "#ROJO_ERROR",
  "errorContainer": "#FONDO_ERROR",
  "success": "#VERDE_EXITO",
  "successContainer": "#FONDO_EXITO",
  "warning": "#AMARILLO_AVISO",
  "warningContainer": "#FONDO_AVISO",
  "info": "#AZUL_INFO",
  "infoContainer": "#FONDO_INFO",

  "outline": "#BORDES",
  "outlineVariant": "#BORDES_SUTILES",
  "scrim": "#SCRIM",
  "shadow": "#SOMBRA",

  "inverseSurface": "#INVERSO_FONDO",
  "inverseOnSurface": "#INVERSO_TEXTO",
  "inversePrimary": "#INVERSO_PRIMARIO"
}
```

> 💡 **Tip**: Usa [coolors.co](https://coolors.co) o [material.io/design/color](https://m3.material.io/styles/color/the-color-system/color-roles) para generar paletas Material 3 coherentes.

#### 🔤 Tipografía (`typography`)

```json
"typography": {
  "fontFamily": "sans-serif",           // Fuente base
  "fontFamilyBold": "sans-serif-medium",
  "fontFamilyBlack": "sans-serif-black",

  "displayLargeSize": 57,
  "displayLargeWeight": "Black",
  "displayMediumSize": 45,
  "displayMediumWeight": "Black",
  "displaySmallSize": 36,
  "displaySmallWeight": "Bold",

  "headlineLargeSize": 32,
  "headlineLargeWeight": "Bold",
  "headlineMediumSize": 28,
  "headlineMediumWeight": "Bold",
  "headlineSmallSize": 24,
  "headlineSmallWeight": "SemiBold",

  "titleLargeSize": 22,
  "titleLargeWeight": "SemiBold",
  "titleMediumSize": 16,
  "titleMediumWeight": "Medium",
  "titleSmallSize": 14,
  "titleSmallWeight": "Medium",

  "bodyLargeSize": 16,
  "bodyLargeWeight": "Normal",
  "bodyMediumSize": 14,
  "bodyMediumWeight": "Normal",
  "bodySmallSize": 12,
  "bodySmallWeight": "Normal",

  "labelLargeSize": 14,
  "labelLargeWeight": "Medium",
  "labelMediumSize": 12,
  "labelMediumWeight": "Medium",
  "labelSmallSize": 11,
  "labelSmallWeight": "Normal"
}
```

> 💡 **Fuentes personalizadas**: Agrega tus `.ttf/.otf` a `app/src/main/res/font/` y usa el nombre del archivo (sin extensión) en `fontFamily`.

#### 🔲 Formas (`shapes`)

```json
"shapes": {
  "cornerExtraSmall": 4,    // Chips, badges
  "cornerSmall": 8,         // Botones, inputs
  "cornerMedium": 12,       // Cards, dialogs
  "cornerLarge": 16,        // Sheets, banners
  "cornerExtraLarge": 28,   // Pantallas completas
  "cornerFull": 9999        // Totalmente redondo (píldoras)
}
```

#### 📐 Layout (`layout`)

```json
"layout": {
  "sidebarWidth": 140,           // Ancho sidebar TV (dp)
  "contentPadding": 24,          // Padding contenido principal
  "cardAspectRatio": 0.727,      // Ancho/Alto cards (160/220)
  "featuredBannerHeight": 280,   // Alto banner destacado
  "itemSpacing": 12,             // Espacio entre items
  "sectionSpacing": 24           // Espacio entre secciones
}
```

#### 🎛️ Feature Flags (`features`)

Activa/desactiva pantallas completas:

```json
"features": {
  "enableLiveTv": true,
  "enableMovies": true,
  "enableSeries": true,
  "enableSearch": true,
  "enableUserProfile": true,
  "enableSettings": true,
  "enableAbout": true,
  "enableLogin": true,
  "enableOfflineMode": false,
  "enableDownload": false,
  "enableCast": false,
  "enableDrm": false,
  "enableAnalytics": false,
  "enableCrashlytics": false
}
```

---

## 🛠️ Compilar localmente

### Linux/macOS (bash)

```bash
# Dar permisos (solo la primera vez)
chmod +x build-theme.sh

# Compilar tema rojo en debug
./build-theme.sh theme-red.json debug -red

# Compilar tema azul en release
./build-theme.sh theme-blue.json release -blue

# Compilar tu tema personalizado
./build-theme.sh theme-mi-marca.json debug -mimarca
```

### Windows (cmd/PowerShell)

```cmd
REM Compilar tema rojo en debug
build-theme.bat theme-red.json debug -red

REM Compilar tu tema en release
build-theme.bat theme-mi-marca.json release -mimarca
```

### Qué hace el script

1. Copia tu tema → `theme-config.json` (activo)
2. Ejecuta `./gradlew assembleDebug` o `assembleRelease`
3. Genera APK en `app/build/outputs/apk/debug/` o `release/`
4. Si pasas sufijo (`-red`), lo añade al `versionName`

---

## ☁️ Compilar en GitHub Actions (gratis, sin instalar nada)

### Opción A: Workflow manual (recomendado)

1. Ve a **Actions → Build APK (Custom Themes) → Run workflow**
2. Selecciona:
   - **Theme config**: `theme-red.json`, `theme-blue.json`, etc.
   - **Build type**: `debug` o `release`
   - **Version suffix**: `-red`, `-mimarca`, etc.
3. Click **Run workflow**
4. Espera 2-5 min → Descarga el artifact `xuper-hydra-*-apk`

### Opción B: Push automático

Cualquier push a `main` que modifique `app/src/main/assets/theme-*.json` o el código de tema compila **todos los temas en paralelo** (matrix strategy).

---

## 📱 Instalar en tu TV Box

```bash
# Debug (firma debug de Android, requiere desinstalar versión anterior)
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Release (firmada con tu keystore, actualiza sin desinstalar)
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## 🎯 Ejemplos de temas listos

| Archivo | Estilo | Uso recomendado |
|---------|--------|-----------------|
| `theme-config.json` | **Xuper Hydra original** (rojo Netflix) | Default |
| `theme-red.json` | Rojo intenso, acento dorado | Marca tipo Netflix |
| `theme-blue.json` | Azul corporativo, acento cyan | Marca tipo Disney+/HBO |
| `theme-green.json` | Verde "Spotify", acento lima | Marca deportiva/naturaleza |
| `theme-purple.json` | Púrpura premium, acento rosa | Marca entretenimiento |
| `theme-dark.json` | Blanco/negro puro, minimalista | Accesibilidad, OLED |

---

## 🔧 Tips avanzados

### Hot-reload del tema en tiempo de ejecución

```kotlin
// En cualquier Activity o Composable
XuperThemeHolder.reload(this, "theme-otro.json")
// La app se recarga con el nuevo tema (requiere reiniciar Activity)
```

### Acceder a colores del tema en código

```kotlin
@Composable
fun MiComponente() {
    val colors = MaterialTheme.colorScheme
    val primary = colors.primary          // Tu brandPrimary
    val surface = colors.surface          // Tu surface
    val onSurface = colors.onSurface      // Tu onSurface
    
    // Colores custom vía extension
    val brandAccent = xuperColors().tertiary  // Tu brandAccent
}
```

### Usar tu tema en previews (Android Studio)

```kotlin
@Preview
@Composable
fun MiPreview() {
    XuperHydraTheme {  // Usa theme-config.json por defecto
        MiComponente()
    }
}

@Preview
@Composable
fun MiPreviewConTema() {
    val config = ThemeConfig.fromJson("""{ "colors": { "brandPrimary": "#00FF00" } }""")
    // O cargar desde assets de test
}
```

---

## 📦 Distribuir tu tema

1. Crea `theme-tu-marca.json` en `app/src/main/assets/`
2. Commit & push
3. GitHub Actions compila automáticamente
4. Comparte el APK o el release de GitHub

¡Listo! Tu marca, tu app, compilada en la nube gratis. 🚀