#!/bin/bash
# ============================================================
# XUPER HYDRA - Script de build local para temas personalizados
# Uso: ./build-theme.sh [tema] [tipo] [sufijo]
# Ejemplos:
#   ./build-theme.sh theme-red.json debug -red
#   ./build-theme.sh theme-blue.json release -blue
#   ./build-theme.sh theme-config.json debug
# ============================================================

set -e

THEME_FILE="${1:-theme-config.json}"
BUILD_TYPE="${2:-debug}"
VERSION_SUFFIX="${3:-}"

THEME_PATH="app/src/main/assets/$THEME_FILE"

echo "🎨 XUPER HYDRA - Build de tema personalizado"
echo "============================================="
echo "Tema: $THEME_FILE"
echo "Tipo: $BUILD_TYPE"
echo "Sufijo: $VERSION_SUFFIX"
echo ""

# Verificar que el tema existe
if [ ! -f "$THEME_PATH" ]; then
    echo "❌ Error: No se encontró $THEME_PATH"
    echo "Temas disponibles:"
    ls app/src/main/assets/theme-*.json 2>/dev/null || echo "  (ninguno)"
    exit 1
fi

# Copiar tema seleccionado como activo
echo "📋 Copiando $THEME_FILE como theme-config.json activo..."
cp "$THEME_PATH" "app/src/main/assets/theme-config.json"

# Verificar gradlew
if [ ! -f "./gradlew" ]; then
    echo "❌ Error: No se encontró gradlew. Ejecuta desde la raíz del proyecto."
    exit 1
fi

# Dar permisos
chmod +x ./gradlew

# Construir comando gradle
GRADLE_TASK="assemble"
if [ "$BUILD_TYPE" = "release" ]; then
    GRADLE_TASK="assembleRelease"
else
    GRADLE_TASK="assembleDebug"
fi

echo "🔨 Compilando $GRADLE_TASK..."

# Ejecutar build
if [ -n "$VERSION_SUFFIX" ]; then
    ./gradlew $GRADLE_TASK -PversionSuffix="$VERSION_SUFFIX" --no-daemon
else
    ./gradlew $GRADLE_TASK --no-daemon
fi

# Mostrar resultados
echo ""
echo "✅ Build completado!"
echo ""

if [ "$BUILD_TYPE" = "release" ]; then
    APK_PATH="app/build/outputs/apk/release"
else
    APK_PATH="app/build/outputs/apk/debug"
fi

echo "📦 APKs generadas:"
ls -lh "$APK_PATH"/*.apk 2>/dev/null || echo "  (no se encontraron APKs)"

echo ""
echo "📱 Para instalar en tu TV Box:"
echo "   adb install -r $APK_PATH/*.apk"