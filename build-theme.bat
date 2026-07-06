@echo off
REM ============================================================
REM XUPER HYDRA - Script de build local para temas personalizados (Windows)
REM Uso: build-theme.bat [tema] [tipo] [sufijo]
REM Ejemplos:
REM   build-theme.bat theme-red.json debug -red
REM   build-theme.bat theme-blue.json release -blue
REM   build-theme.bat theme-config.json debug
REM ============================================================

set THEME_FILE=%~1
set BUILD_TYPE=%~2
set VERSION_SUFFIX=%~3

if "%THEME_FILE%"=="" set THEME_FILE=theme-config.json
if "%BUILD_TYPE%"=="" set BUILD_TYPE=debug

set THEME_PATH=app\src\main\assets\%THEME_FILE%

echo 🎨 XUPER HYDRA - Build de tema personalizado
echo =============================================
echo Tema: %THEME_FILE%
echo Tipo: %BUILD_TYPE%
echo Sufijo: %VERSION_SUFFIX%
echo.

REM Verificar que el tema existe
if not exist "%THEME_PATH%" (
    echo ❌ Error: No se encontró %THEME_PATH%
    echo Temas disponibles:
    dir /b app\src\main\assets\theme-*.json 2>nul || echo   (ninguno)
    exit /b 1
)

REM Copiar tema seleccionado como activo
echo 📋 Copiando %THEME_FILE% como theme-config.json activo...
copy /Y "%THEME_PATH%" "app\src\main\assets\theme-config.json" >nul

REM Verificar gradlew
if not exist "gradlew.bat" (
    echo ❌ Error: No se encontró gradlew.bat. Ejecuta desde la raíz del proyecto.
    exit /b 1
)

REM Construir comando gradle
set GRADLE_TASK=assemble
if "%BUILD_TYPE%"=="release" (
    set GRADLE_TASK=assembleRelease
) else (
    set GRADLE_TASK=assembleDebug
)

echo 🔨 Compilando %GRADLE_TASK%...

REM Ejecutar build
if not "%VERSION_SUFFIX%"=="" (
    gradlew.bat %GRADLE_TASK% -PversionSuffix="%VERSION_SUFFIX%" --no-daemon
) else (
    gradlew.bat %GRADLE_TASK% --no-daemon
)

echo.
echo ✅ Build completado!
echo.

if "%BUILD_TYPE%"=="release" (
    set APK_PATH=app\build\outputs\apk\release
) else (
    set APK_PATH=app\build\outputs\apk\debug
)

echo 📦 APKs generadas:
dir "%APK_PATH%\*.apk" 2>nul || echo   (no se encontraron APKs)

echo.
echo 📱 Para instalar en tu TV Box:
echo    adb install -r %APK_PATH%\*.apk