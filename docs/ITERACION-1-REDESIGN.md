# Iteración 1 — Rediseño visual + proyecto nuevo

> Fecha: 2026-07-02  
> Estado: ✅ Completada

## Cambios aplicados a la APK original

### `colors.xml`
- Override de 8 colores de marca existentes:
  - `color_main`: `#c91923` → `#e50914` (rojo Xuper brillante)
  - `color_main_66`: `#66c91923` → `#66e50914`
  - `color_0b0b0b`: `#ff0b0b0b` → `#ff0a0a0a` (negro más profundo)
  - `color_1a1a1a`: `#ff1a1a1a` → `#ff141414`
  - `color_app_name`, `color_b3b3b3`, `color_font_main`: blanco/gris (sin cambio)
  - `color_ff3333`: `#ff3333` → `#e50914` (unificado con marca)
- 16 colores nuevos agregados: `color_main_pressed`, `color_main_dim`, `color_bg_*`, `color_text_*`, `color_divider`, `color_border_focus`, `color_success/warning/error/info`.

### `styles.xml`
- `BaseAppTheme` migrado de `Theme.AppCompat.Light.NoActionBar` → `Theme.AppCompat.NoActionBar` (elimina parpadeo blanco).
- Agregados: `windowBackground` (negro), `colorBackground`, `colorPrimary`, `colorPrimaryDark`, `colorAccent`, `textColorPrimary`, `textColorSecondary`.

### `values-es/strings.xml`
- 47 traducciones nuevas agregadas:
  - `app_name`, `home_category_*` (8 claves), `error_code_*` (9 claves), `scan_qr*` (3 claves), `settings`, `play_setting_audio`, `N_A`, `Ignore`, `a_cache`
  - 24 claves técnicas: `TrackType_*`, `VideoView_*`

### `values-en/strings.xml`
- Creado como copia explícita del inglés (para auto-detección de idioma).

## Recompilación

```bash
# 1. Recompilar recursos
java -jar tools/apktool.jar b decompiled/xuper_apktool \
  -o download/xuper-redesign-unsigned.apk --use-aapt2

# 2. Zipalign
build-tools/34.0.0/zipalign -p -f -v 4 \
  download/xuper-redesign-unsigned.apk \
  download/xuper-redesign-aligned.apk

# 3. Generar keystore nuevo (auto-firmado)
keytool -genkeypair \
  -keystore download/xuper-redesign-keystore.jks \
  -alias xuper-redesign -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass xuper123 -keypass xuper123 \
  -dname "CN=XuperRedesign, OU=Dev, O=XuperHydra, L=Bogota, ST=Cundinamarca, C=CO"

# 4. Firmar
build-tools/34.0.0/apksigner sign \
  --ks download/xuper-redesign-keystore.jks \
  --ks-key-alias xuper-redesign \
  --ks-pass pass:xuper123 --key-pass pass:xuper123 \
  --out download/xuper-redesign.apk \
  download/xuper-redesign-aligned.apk

# 5. Verificar
build-tools/34.0.0/apksigner verify --verbose download/xuper-redesign.apk
# → Verified v1: true, v2: true, v3: true
```

## Proyecto Android Studio nuevo

Generado en `app/` con stack moderno:
- **Kotlin 2.0.20** + **Jetpack Compose** + **Material 3**
- 11 activities implementadas (ver [`README.md`](../README.md))
- Tema `Theme.Material3.Dark.NoActionBar` con paleta streaming oscuro
- Strings bilingües ES/EN completos + auto-detección
- Selector de idioma en `SettingsActivity` (persiste en SharedPreferences)

## Diferencias vs APK original

| Aspecto | Original | Nuevo |
|---|---|---|
| Lenguaje | Java | Kotlin |
| UI | XML layouts (518 archivos) | Jetpack Compose |
| Tema | `Theme.AppCompat.Light` (parpadeo blanco) | `Theme.Material3.Dark` |
| Paleta marca | `#c91923` (rojo apagado) | `#E50914` (rojo brillante) |
| Reproductor | IJKPlayer + ExoPlayer antiguo | Media3 / ExoPlayer 1.4 |
| Idiomas | ES parcial (220 strings faltantes) | ES + EN completos |
| Imágenes | Glide | Coil (Compose-friendly) |
| Navegación | TheRouter | Navigation Compose |
| AndroidX | AppCompat 1.x | AppCompat 1.7 + Compose BOM 2024.09 |
| targetSdk | 33 | 34 |
