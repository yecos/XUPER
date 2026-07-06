# XUPER — Rediseño de Xuper Hydra

Proyecto de rediseño y mejora de la app Android **Xuper Hydra** (IPTV/VOD para Android TV).

> ⚠️ **Aviso**: Este proyecto se basa en una APK de terceros compartida por el usuario con fines educativos. No redistribuir la APK modificada.

---

## 📦 Estructura del repo

```
XUPER/
├── .github/workflows/                  ← CI/CD con GitHub Actions
│   ├── build-apk.yml                   ← Build debug en cada push
│   └── release-apk.yml                 ← Release firmada en tags
├── app/                                ← Proyecto Android Studio
│   ├── build.gradle.kts                ← Configuración Gradle + signing
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/xuper/netxxus/
│       │   ├── XuperApp.kt
│       │   ├── ui/                      ← 11 activities Compose
│       │   └── data/
│       │       ├── api/
│       │       │   ├── XuperApi.kt      ← Interfaz Retrofit (esqueleto)
│       │       │   └── ApiClient.kt     ← Cliente + interceptores
│       │       └── model/
│       │           └── Models.kt        ← Data classes (esqueleto)
│       └── res/                        ← Paleta streaming oscuro + ES/EN
├── tools/
│   ├── mitmproxy/                      ← (vacío) descarga certificados aquí
│   ├── frida/
│   │   └── ssl-bypass.js               ← Script bypass SSL pinning
│   └── analyze_traffic.py              ← Analiza .flow/.har y extrae endpoints
├── docs/
│   ├── ANALISIS-APK-ORIGINAL.md
│   ├── ITERACION-1-REDESIGN.md
│   └── CAPTURA-TRAFICO.md              ← Guía completa mitmproxy + Frida
├── scripts/
│   └── apply_redesign.py
├── gradle/wrapper/
├── gradlew, gradlew.bat
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── .gitignore
```

---

## 🔬 Captura de tráfico con mitmproxy (para wire-up real)

Para que la app funcione con el backend real de Xuper, necesitamos capturar
los endpoints del backend original. Setup completo en [`docs/CAPTURA-TRAFICO.md`](docs/CAPTURA-TRAFICO.md).

**Resumen del flujo:**
1. Instalar mitmproxy en el PC
2. Instalar certificado CA en el dispositivo Android (root)
3. Configurar proxy en Android
4. Bypass SSL pinning con Frida (script `tools/frida/ssl-bypass.js`)
5. Navegar la app original y capturar tráfico
6. Ejecutar `python3 tools/analyze_traffic.py captura.flow`
7. Pegar endpoints en `app/src/main/java/com/xuper/netxxus/data/api/XuperApi.kt`

---

## 🎨 Sistema de Temas Personalizables (NUEVO)

XUPER HYDRA incluye un **sistema completo de temas** que te permite cambiar la "cara" visual de la app (colores, tipografía, formas, layout, features) **sin tocar código Kotlin**.

### 🚀 Inicio rápido

```bash
# Ver temas disponibles
ls app/src/main/assets/theme-*.json

# Compilar tema rojo localmente
./build-theme.sh theme-red.json debug -red

# O en Windows
build-theme.bat theme-red.json debug -red
```

### ☁️ Compilar en GitHub Actions (gratis)

1. Ve a **Actions → Build APK (Custom Themes) → Run workflow**
2. Elige tu tema (`theme-red.json`, `theme-blue.json`, tu propio `theme-mi-marca.json`)
3. Elige `debug` o `release`
4. Click **Run workflow** → Descarga el APK en Artifacts

### 📖 Documentación completa

Ver [`docs/TEMAS-PERSONALIZABLES.md`](docs/TEMAS-PERSONALIZABLES.md) para:
- Estructura del JSON de tema
- Cómo crear tu propia paleta Material 3
- Feature flags para activar/desactivar pantallas
- Hot-reload en tiempo de ejecución
- Previews en Android Studio

### Temas incluidos

| Archivo | Estilo |
|---------|--------|
| `theme-config.json` | Xuper Hydra original (rojo Netflix) |
| `theme-red.json` | Rojo intenso + acento dorado |
| `theme-blue.json` | Azul corporativo + cyan |
| `theme-green.json` | Verde "Spotify" + lima |
| `theme-purple.json` | Púrpura premium + rosa |
| `theme-dark.json` | Blanco/negro puro minimalista |

---

## 🤖 Compilar la APK con GitHub Actions (sin instalar nada)

Tienes dos workflows configurados:

### 1. **Build Debug** — automático en cada push

Cada vez que haces `git push` a `main`, GitHub Actions compila automáticamente
una APK debug y la publica como **artifact** descargable.

**Pasos:**
1. Haz push de tus cambios
2. Ve a https://github.com/yecos/XUPER/actions
3. Espera a que termine el workflow "Build APK (debug)"
4. Haz clic en la ejecución → descarga el artifact `xuper-hydra-debug-apk`
5. ¡Instala la APK en tu TV Box!

> La APK debug está firmada con el keystore debug de Android (no necesita configuración).

### 2. **Release Firmada** — en cada tag `v*.*.*`

Para generar una APK release firmada (instalable sin desinstalar versiones debug anteriores),
necesitas configurar **4 secrets** en GitHub con tu keystore:

#### Paso 1 — Generar keystore local (una sola vez)
```bash
keytool -genkeypair \
  -keystore xuper-release.keystore \
  -alias xuper -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass TU_PASSWORD -keypass TU_PASSWORD \
  -dname "CN=XuperHydra, OU=Dev, O=XuperHydra, L=Bogota, ST=Cundinamarca, C=CO"
```

> ⚠️ **Guarda este `.jks` en lugar seguro**. Si lo pierdes no podrás actualizar la app.

#### Paso 2 — Subir secrets a GitHub
Ve a https://github.com/yecos/XUPER/settings/secrets/actions y agrega:

| Secret | Valor |
|---|---|
| `SIGNING_KEYSTORE_BASE64` | Salida de `base64 -w0 xuper-release.keystore` |
| `SIGNING_STORE_PASSWORD` | Password del keystore |
| `SIGNING_KEY_ALIAS` | `xuper` (o el alias que elegiste) |
| `SIGNING_KEY_PASSWORD` | Password de la clave |

Para generar el base64 del keystore:
```bash
# Linux/Mac
base64 -w0 xuper-release.keystore

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("xuper-release.keystore"))
```

#### Paso 3 — Disparar el release
```bash
git tag v4.35.1
git push origin v4.35.1
```
O desde la web: **Actions tab → Release APK (signed) → Run workflow**.

GitHub Actions hará todo automáticamente:
1. Compilará la APK release
2. La firmará con tu keystore
3. Creará un GitHub Release con la APK adjunta

### 3. **Compilar localmente** (alternativa)

Si prefieres compilar en tu PC con Android Studio:

1. Clona el repo:
   ```bash
   git clone https://github.com/yecos/XUPER.git
   ```
2. Abre la carpeta en **Android Studio Hedgehog (2023.1.1) o superior**
3. Espera a que Gradle sincronice (5–10 min la primera vez)
4. **Run → Run 'app'** (debug) o **Build → Generate Signed Bundle / APK** (release)

Para release local, crea un archivo `keystore.properties` en la raíz del proyecto:
```properties
storeFile=/ruta/absoluta/a/xuper-release.keystore
storePassword=TU_PASSWORD
keyAlias=xuper
keyPassword=TU_PASSWORD
```
> Ya está en `.gitignore`, no se subirá al repo.

---

## 🎨 Paleta de marca — Streaming Oscuro

| Uso | Color | Hex |
|---|---|---|
| Marca / CTA | Rojo Xuper | `#E50914` |
| Fondo principal | Negro profundo | `#0A0A0A` |
| Fondo secundario (cards) | Gris carbón | `#141414` |
| Fondo hover/focus | Gris medio | `#1F1F1F` |
| Texto principal | Blanco puro | `#FFFFFF` |
| Texto secundario | Gris claro | `#B3B3B3` |
| Estado: éxito | Verde | `#46D369` |
| Estado: error | Rojo | `#E50914` |

---

## 🌐 Idiomas

La app es **bilingüe ES/EN con auto-detección**:
- Por defecto respeta el idioma del sistema (auto)
- El usuario puede cambiar manualmente en `SettingsActivity`
- La preferencia se guarda en `SharedPreferences("xuper_prefs")` clave `language`
  - `"es"` = español forzado
  - `"en"` = inglés forzado
  - ausente = auto (sistema)

---

## 📋 Estado actual

### ✅ Iteración 1 (completada)
- Rediseño visual MD3 + paleta streaming oscuro
- Tema oscuro coherente (sin parpadeo blanco)
- 47 strings ES traducidos
- 11 activities Compose implementadas
- Proyecto Android Studio funcional
- APK rediseñada compilada y firmada (ver Releases)
- **CI/CD con GitHub Actions configurado**

### 🔜 Próximas iteraciones sugeridas
- **Iteración 2**: Integrar ExoPlayer real en `PlayerActivity`
- **Iteración 3**: Wire-up de la API (reemplazar mock data)
- **Iteración 4**: Portar más drawables desde la APK original
- **Iteración 5**: Adaptive icon (Android 8+)
- **Iteración 6**: Soporte 4K real (layouts responsivos)

---

## 🔐 Notas de seguridad

- **El keystore NUNCA se commitea** (`.gitignore` excluye `*.jks`, `*.keystore`, `keystore.properties`).
- Los secrets se configuran en GitHub Settings → Secrets and variables → Actions.
- La APK release está firmada con tu keystore personal → solo tú puedes publicar updates.
- La app permite cleartext traffic (`network_security_config.xml`) — mismo comportamiento que la original.

---

## 📚 Documentación adicional

- [`docs/ANALISIS-APK-ORIGINAL.md`](docs/ANALISIS-APK-ORIGINAL.md) — Análisis técnico completo de la APK original
- [`docs/ITERACION-1-REDESIGN.md`](docs/ITERACION-1-REDESIGN.md) — Detalle de los cambios de la iteración 1
- [`docs/TEMAS-PERSONALIZABLES.md`](docs/TEMAS-PERSONALIZABLES.md) — **Guía completa del sistema de temas personalizables**
- [`scripts/apply_redesign.py`](scripts/apply_redesign.py) — Script que aplica el rediseño a la APK decompilada
- [**Actions tab**](https://github.com/yecos/XUPER/actions) — Ver builds en curso y descargar artifacts
