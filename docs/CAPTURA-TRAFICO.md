# Captura de tráfico de Xuper Hydra con mitmproxy

Guía paso a paso para capturar el tráfico HTTPS de la APK original de Xuper Hydra,
extraer los endpoints reales del backend y usarlos en el proyecto rediseñado.

> ⚠️ **Requisitos previos**:
> - Dispositivo Android **rooteado** (TV Box, Fire Stick con rooting, o móvil rooteado)
> - Android 5.0+ (la app requiere minSdk 21)
> - PC con ADB instalado
> - Misma red WiFi entre PC y dispositivo Android

---

## 📋 Resumen del flujo

1. Instalar mitmproxy en el PC
2. Generar e instalar el certificado CA de mitmproxy en el dispositivo Android (como system cert, requiere root)
3. Configurar el dispositivo Android para usar el PC como proxy HTTP
4. (Opcional) Instalar Frida + script de bypass SSL pinning (si la app tiene pinning)
5. Abrir la app original Xuper Hydra y navegar por todas las pantallas
6. Exportar la captura como `.har` o `.flow`
7. Ejecutar el script `analyze_traffic.py` para extraer endpoints automáticamente
8. Pegar los endpoints en la interfaz Retrofit del proyecto

---

## 1️⃣ Instalar mitmproxy en el PC

### Linux / macOS
```bash
# Opción A: pip
pip install --user mitmproxy

# Opción B: brew (macOS)
brew install mitmproxy

# Opción C: binario precompilado
# https://mitmproxy.org/downloads/
```

### Windows
Descarga el instalador de https://mitmproxy.org/downloads/ y ejecútalo.

Verifica:
```bash
mitmproxy --version
mitmdump --version
```

---

## 2️⃣ Descubrir la IP del PC

```bash
# Linux
ip addr | grep "inet " | grep -v 127.0.0.1

# macOS
ifconfig | grep "inet " | grep -v 127.0.0.1

# Windows
ipconfig
```

Anota la IP (ej. `192.168.1.50`). El proxy correrá en `http://TU_IP:8080`.

---

## 3️⃣ Iniciar mitmproxy

En una terminal del PC, ejecuta:

```bash
# Opción A: TUI interactiva (recomendada para ver tráfico en vivo)
mitmproxy --listen-port 8080

# Opción B: headless, guarda todo a un archivo
mitmdump --listen-port 8080 -w xuper-capture.flow

# Opción C: web UI (útil si prefieres navegador)
mitmweb --listen-port 8080
```

La primera vez que se ejecute, mitmproxy creará su CA en `~/.mitmproxy/`.

---

## 4️⃣ Generar e instalar el certificado CA

### 4.1 Generar el certificado (en el PC)

Si no existe aún, simplemente ejecuta `mitmproxy` una vez y se creará solo.
Luego cancela con `Ctrl+C`.

```bash
ls ~/.mitmproxy/
# mitmproxy-ca-cert.cer   ← este es el que vamos a instalar
# mitmproxy-ca-cert.pem
# mitmproxy-ca-key.pem
```

### 4.2 Calcular el hash del certificado (Android < 14)

Android espera que los system certs tengan como nombre de archivo el hash del sujeto X.509 + `.0`.

```bash
# En el PC
openssl x509 -inform PEM -subject_hash_old -in ~/.mitmproxy/mitmproxy-ca-cert.pem | head -1
# Devuelve algo como: c8750f0d
```

### 4.3 Instalar el certificado como **system cert** en Android (requiere root)

```bash
# Conecta el dispositivo con ADB
adb devices

# Subir el certificado al dispositivo
adb push ~/.mitmproxy/mitmproxy-ca-cert.pem /sdcard/mitmproxy-ca-cert.pem

# Entrar al dispositivo como root
adb shell
su

# Dentro del shell del dispositivo (como root):
# 1. Calcular hash (ejecutar en el dispositivo)
HASH=$(openssl x509 -inform PEM -subject_hash_old -in /sdcard/mitmproxy-ca-cert.pem | head -1)
echo "Hash: $HASH"

# 2. Montar /system como escritura
mount -o rw,remount /system
# En algunos dispositivos Android 10+ es:
# mount -o rw,remount /

# 3. Copiar el certificado con el nombre correcto
cp /sdcard/mitmproxy-ca-cert.pem /system/etc/security/cacerts/${HASH}.0
chmod 644 /system/etc/security/cacerts/${HASH}.0

# 4. (Opcional en Magisk) Si /system es read-only incluso con root, usar Magisk:
#    - Crear módulo Magisk con: /system/etc/security/cacerts/$HASH.0

# 5. Desmontar y salir
mount -o ro,remount /system
exit
exit
```

> 🔧 **Alternativa más simple con Magisk** (recomendado en Android 10+):
> Instala el módulo **"MagiskTrustUserCerts"** desde Magisk Manager. Esto copia automáticamente los certs de usuario al system store al arrancar. Luego solo necesitas instalar el certificado vía Settings → Security → Install from storage.

### 4.4 Verificar que el certificado está instalado

En el dispositivo: **Settings → Security → Encryption & credentials → Trusted credentials → System** → busca "mitmproxy".

---

## 5️⃣ Configurar el proxy en Android

### Opción A: vía Settings (WiFi)

1. En Android: **Settings → Network → WiFi** → mantén pulsada tu red → **Modify network**
2. Marca **Advanced options**
3. **Proxy**: Manual
4. **Proxy hostname**: IP de tu PC (ej. `192.168.1.50`)
5. **Proxy port**: `8080`
6. Guarda

### Opción B: vía ADB (más rápido)

```bash
adb shell settings put global http_proxy 192.168.1.50:8080

# Para quitar el proxy después:
adb shell settings put global http_proxy :0
```

### Opción C: para TV Box / Fire Stick sin UI de proxy

```bash
# Algunos TV Box no exponen el setting de proxy. Usar iptables:
adb shell
su
iptables -t nat -A OUTPUT -p tcp --dport 80 -j DNAT --to-destination 192.168.1.50:8080
iptables -t nat -A OUTPUT -p tcp --dport 443 -j DNAT --to-destination 192.168.1.50:8080
```

---

## 6️⃣ Verificar captura básica (sin SSL pinning aún)

1. Abre el navegador Chrome del dispositivo y visita `https://example.com`
2. En mitmproxy deberías ver la petición `GET https://example.com/`
3. Si la ves, ✅ proxy + cert funcionan

Si ves el tráfico pero las apps fallan con errores SSL → necesitas bypass de SSL pinning (siguiente sección).

---

## 7️⃣ Bypass SSL pinning con Frida (si la app tiene pinning)

Xuper Hydra usa OkHttp3 + CertificatePinner (muy probable). Para bypassearlo:

### 7.1 Instalar Frida en el PC

```bash
pip install --user frida-tools
```

Verifica:
```bash
frida --version
```

### 7.2 Instalar Frida server en el dispositivo

```bash
# Descargar frida-server que coincida con tu arquitectura y versión
# https://github.com/frida/frida/releases

# Detectar arquitectura del dispositivo
adb shell getprop ro.product.cpu.abi
# Ej: arm64-v8a

# Descargar frida-server (ejemplo: 17.0.0 para arm64)
wget https://github.com/frida/frida/releases/download/17.0.0/frida-server-17.0.0-android-arm64.xz
unxz frida-server-17.0.0-android-arm64.xz

# Subir al dispositivo
adb push frida-server-17.0.0-android-arm64 /data/local/tmp/frida-server
adb shell
su
chmod +x /data/local/tmp/frida-server
/data/local/tmp/frida-server &
exit
exit
```

Verifica desde el PC:
```bash
frida-ps -U | head -20
# Deberías ver la lista de procesos del dispositivo
```

### 7.3 Script de bypass SSL pinning para Xuper Hydra

El archivo [`tools/frida/ssl-bypass.js`](../tools/frida/ssl-bypass.js) incluye bypass para:

- OkHttp3 `CertificatePinner`
- TrustManager personalizado
- `SSLContext` / `SSLSocketFactory`
- Conscrypt (Android 8+)
- Apache HTTP (legacy)

Ejecutar con la app ya abierta:

```bash
# Spawn + bypass (abre la app con frida ya inyectado)
frida -U -f com.xuper.netxxus -l tools/frida/ssl-bypass.js --no-pause

# O attach a una app ya abierta
frida -U -n "Xuper Hydra" -l tools/frida/ssl-bypass.js
```

Deberías ver en la consola:
```
[*] SSL pinning bypass loaded
[+] OkHttp3 CertificatePinner.check() bypassed
[+] TrustManager bypassed
[+] SSLContext bypassed
```

---

## 8️⃣ Capturar el tráfico

Con mitmproxy corriendo + Frida inyectado + proxy configurado:

1. Abre la app **Xuper Hydra** en el dispositivo
2. Espera a que cargue el splash → login
3. **Navega por TODAS las pantallas**:
   - Login con tu cuenta (o prueba como visitante si es posible)
   - Home → recorre cada categoría
   - Películas → entra al detalle de varias
   - Series → entra al detalle de varias
   - Live TV → recorre canales
   - Búsqueda → busca algo
   - Player → reproduce un canal o película
   - Settings → abre cada sección
4. Mientras navegas, mitmproxy va capturando todo

### Guardar la captura

Si usaste `mitmproxy` (TUI):
- Presiona `E` → elige formato → guarda como `xuper-capture.flow` o `.har`

Si usaste `mitmdump -w xuper-capture.flow`:
- Ya se está guardando. `Ctrl+C` para terminar.

Si usaste `mitmweb`:
- Botón "File → Save as" en la web UI

---

## 9️⃣ Analizar la captura

Sube el archivo `.flow` o `.har` al repo (no al git, vía issue o drive), luego:

```bash
python3 tools/analyze_traffic.py xuper-capture.flow
```

El script:
- Lista todas las URLs únicas capturadas
- Identifica las que parecen ser de la API de Xuper (filtra Google/Umeng/Firebase)
- Extrae métodos HTTP, headers y bodies
- Genera un reporte Markdown con todos los endpoints
- Sugiere nombres para la interfaz Retrofit

---

## 🔟 Mapear endpoints a la interfaz Retrofit

Una vez tengas el reporte con los endpoints reales, edita:

- `app/src/main/java/com/xuper/netxxus/data/api/XuperApi.kt` — interfaz Retrofit
- `app/src/main/java/com/xuper/netxxus/data/model/Models.kt` — data classes

Y conecta cada endpoint a su activity correspondiente.

---

## 🐛 Problemas comunes

### "No veo ninguna petición en mitmproxy"
- Verifica que PC y dispositivo estén en la misma red
- Verifica que el proxy esté configurado correctamente en Android
- Algunas apps ignoran el proxy del sistema → usa iptables (sección 5 opción C)

### "Veo peticiones pero todas fallan con SSL error"
→ La app tiene SSL pinning. Ve a la sección 7.

### "Frida no se conecta al dispositivo"
- Verifica que `frida-server` esté corriendo en el dispositivo: `adb shell ps | grep frida`
- Verifica la versión: `frida --version` en PC debe coincidir con `frida-server` en dispositivo
- Prueba con `frida-ps -U --debug` para ver errores detallados

### "La app se cierra al abrir (con Frida inyectado)"
- Prueba sin `--no-pause`: `frida -U -f com.xuper.netxxus -l tools/frida/ssl-bypass.js`
- Prueba con un bypass más conservador (ver comentarios en el script)
- Revisa logcat: `adb logcat | grep -i "xuper\|frida\|ssl"`

### "El certificado no aparece en Trusted credentials"
- En Android 14+, los system certs deben ir en `/apex/com.android.conscrypt/cacerts/` (más complejo)
- Magisk + módulo "MagiskTrustUserCerts" es la solución más portable

### "Mi TV Box no tiene opción de proxy en Settings"
- Usa `adb shell settings put global http_proxy IP:PORT`
- O iptables (sección 5 opción C)
- O si nada funciona: usa **PC como hotspot WiFi** y captura enrutando el tráfico

---

## 📋 Checklist final

- [ ] mitmproxy instalado en el PC
- [ ] IP del PC anotada (ej. 192.168.1.50)
- [ ] mitmproxy corriendo en puerto 8080
- [ ] Certificado CA generado en `~/.mitmproxy/`
- [ ] Certificado instalado como system cert en Android
- [ ] Proxy configurado en Android
- [ ] Verificado: navegador Chrome puede cargar HTTPS vía proxy
- [ ] Frida server corriendo en el dispositivo
- [ ] Frida client instalado en el PC
- [ ] Script `tools/frida/ssl-bypass.js` descargado
- [ ] App Xuper Hydra original instalada (sin modificar)
- [ ] Captura realizada navegando por todas las pantallas
- [ ] Archivo `.flow` o `.har` guardado
- [ ] `python3 tools/analyze_traffic.py <archivo>` ejecutado
- [ ] Endpoints mapeados a `XuperApi.kt`
- [ ] App rediseñada probada con API real

---

## 🔐 Notas de seguridad y legalidad

- Este procedimiento es para **fines educativos y de interoperabilidad** con una app de la que eres usuario legítimo.
- No redistribuyas la APK original ni las credenciales capturadas.
- Algunas jurisdicciones prohíben bypass de DRM/SSL pinning incluso para uso personal. Verifica la legislación local.
- Si la app usa Widevine DRM (típico en IPTV), NO podrás descifrar el contenido de los streams, solo ver las URLs y metadatos.
