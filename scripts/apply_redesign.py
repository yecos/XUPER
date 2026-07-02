#!/usr/bin/env python3
"""
Xuper Hydra — Iteración 1 v2: Rediseño visual MD3 + Streaming Oscuro + ES completo
CORRECCIÓN: en AAPT2 no se permiten duplicados. Hay que MODIFICAR las definiciones
existentes, no añadir nuevas al final.
"""

import re
from pathlib import Path

APK_RES = Path("/home/z/my-project/decompiled/xuper_apktool/res")

# ============================================================
# Colores legacy → nuevos valores streaming oscuro
# (clave: nombre del color, valor: nuevo hex)
# ============================================================
COLOR_OVERRIDES = {
    # Marca
    "color_main":            "#ffe50914",  # Rojo Xuper brillante (antes #c91923 apagado)
    "color_main_66":         "#66e50914",
    # Fondos
    "color_0b0b0b":          "#ff0a0a0a",  # Antes #0b0b0b → ahora #0a0a0a (más profundo)
    "color_1a1a1a":          "#ff141414",  # Antes #1a1a1a → ahora #141414
    # Texto
    "color_app_name":        "#ffffffff",
    "color_b3b3b3":          "#ffb3b3b3",
    "color_font_main":       "#ffffffff",
    # Acentos que apuntaban a tonos rojos/naranjas → unificar al rojo Xuper
    "color_ff3333":          "#ffe50914",  # Rojo brillante (antes #ff3333)
    "color_ff5500":          "#ffe50914",  # Era naranja #ff5500, ahora rojo Xuper
}

# Nuevos colores a AGREGAR (sin colisión con existentes)
NEW_COLORS = {
    "color_main_pressed":    "#ffb20710",
    "color_main_dim":        "#80e50914",
    "color_bg_primary":      "#ff0a0a0a",
    "color_bg_secondary":    "#ff141414",
    "color_bg_tertiary":     "#ff1f1f1f",
    "color_bg_elevated":     "#ff2a2a2a",
    "color_text_primary":    "#ffffffff",
    "color_text_secondary":  "#ffb3b3b3",
    "color_text_disabled":   "#ff707070",
    "color_text_accent":     "#ffe50914",
    "color_divider":         "#ff2a2a2a",
    "color_border_focus":    "#ffe50914",
    "color_success":         "#ff46d369",
    "color_warning":         "#ffffb700",
    "color_error":           "#ffe50914",
    "color_info":            "#ff2196f3",
}

def update_colors():
    """Modifica los colores existentes y agrega nuevos al final."""
    f = APK_RES / "values/colors.xml"
    content = f.read_text(encoding="utf-8")
    original = content
    
    # 1. Override de colores existentes
    for name, new_hex in COLOR_OVERRIDES.items():
        # Patrón: <color name="color_main">#ffc91923</color>
        pattern = re.compile(
            rf'(<color name="{re.escape(name)}">)[^<]*(</color>)'
        )
        new_content, n = pattern.subn(rf'\g<1>{new_hex}\g<2>', content, count=1)
        if n == 0:
            print(f"  [WARN] color '{name}' no encontrado")
        else:
            content = new_content
            print(f"  [OK] override {name} → {new_hex}")
    
    # 2. Agregar nuevos colores antes de </resources>
    new_block_lines = ["\n    <!-- ===== XUPER HYDRA — Nuevos colores Streaming Oscuro ===== -->"]
    for name, hex_val in NEW_COLORS.items():
        new_block_lines.append(f'    <color name="{name}">{hex_val}</color>')
    new_block = "\n".join(new_block_lines) + "\n"
    
    content = content.replace("</resources>", new_block + "</resources>", 1)
    
    if content != original:
        f.write_text(content, encoding="utf-8")
        print(f"[OK] colors.xml actualizado (override + {len(NEW_COLORS)} nuevos colores)")
    else:
        print("[WARN] colors.xml sin cambios")

# ============================================================
# TEMA — Migrar BaseAppTheme a tema oscuro
# ============================================================
NEW_BASE_THEME = '''    <style name="BaseAppTheme" parent="@style/Theme.AppCompat.NoActionBar">
        <item name="android:windowBackground">@color/color_bg_primary</item>
        <item name="android:colorBackground">@color/color_bg_primary</item>
        <item name="android:windowNoTitle">true</item>
        <item name="android:windowFullscreen">true</item>
        <item name="android:windowSoftInputMode">adjustNothing</item>
        <item name="colorPrimary">@color/color_main</item>
        <item name="colorPrimaryDark">@color/color_bg_primary</item>
        <item name="colorAccent">@color/color_main</item>
        <item name="android:textColorPrimary">@color/color_text_primary</item>
        <item name="android:textColorSecondary">@color/color_text_secondary</item>
        <item name="viewInflaterClass">com.interactive.brasiliptv.utils.CustomLayoutInflater</item>
    </style>'''

OLD_BASE_THEME_PATTERN = re.compile(
    r'    <style name="BaseAppTheme" parent="@style/Theme\.AppCompat\.Light\.NoActionBar">.*?</style>',
    re.DOTALL
)

def update_base_theme():
    f = APK_RES / "values/styles.xml"
    content = f.read_text(encoding="utf-8")
    new_content, n = OLD_BASE_THEME_PATTERN.subn(NEW_BASE_THEME, content, count=1)
    if n == 0:
        print("[WARN] No se encontró BaseAppTheme para reemplazar")
        return
    f.write_text(new_content, encoding="utf-8")
    print(f"[OK] BaseAppTheme migrado a tema oscuro (Theme.AppCompat.NoActionBar)")

# ============================================================
# STRINGS ES — Agregar traducciones faltantes
# ============================================================
ES_TRANSLATIONS = {
    # Branding
    "app_name": "Xuper Hydra",
    # Home categories
    "home_category_all": "Todo",
    "home_category_new": "Novedades",
    "home_category_top": "Populares",
    "home_category_show": "Programas",
    "home_category_sports": "Deportes",
    "home_category_music": "Música",
    "home_category_air": "En vivo",
    "home_category_genera": "General",
    # Error codes
    "error_code_neterror": "Error de red, verifique su conexión",
    "error_code_net_type": "Error de red (%1$s)",
    "error_code_code_type": "Código de error (%1$s)",
    "error_code_play_type": "Error de reproducción (%1$s)",
    "error_code_pcdn_type": "Error de red CDN (%1$s)",
    "error_code_sn_type": "Error de dispositivo (%1$s)",
    "error_code_user_type": "Error de usuario (%1$s)",
    "error_code_product_type": "Error de producto (%1$s)",
    "error_code_cateory_type": "Error de categoría (%1$s)",
    # Scan / QR
    "please_scan": "Por favor escanee",
    "scan_qr": "Escanear código QR",
    "scan_qr_login": "Escanear para iniciar sesión",
    # Settings
    "settings": "Ajustes",
    # Player
    "play_setting_audio": "Pista de audio",
    # Misc
    "N_A": "N/D",
    "Ignore": "Ignorar esta versión",
    "a_cache": "caché",
    # Technical
    "TrackType_audio": "Audio",
    "TrackType_metadata": "Metadatos",
    "TrackType_subtitle": "Subtítulos",
    "TrackType_timedtext": "Texto sincronizado",
    "TrackType_unknown": "Desconocido",
    "TrackType_video": "Vídeo",
    "VideoView_ar_16_9_fit_parent": "16:9 / Ajustar al contenedor",
    "VideoView_ar_4_3_fit_parent": "4:3 / Ajustar al contenedor",
    "VideoView_ar_aspect_fill_parent": "Proporción / Rellenar",
    "VideoView_ar_aspect_fit_parent": "Proporción / Ajustar",
    "VideoView_ar_aspect_wrap_content": "Proporción / Ajustar contenido",
    "VideoView_ar_match_parent": "Libre / Rellenar",
    "VideoView_error_button": "Aceptar",
    "VideoView_error_text_invalid_progressive_playback": "Reproducción progresiva inválida",
    "VideoView_error_text_unknown": "Error desconocido",
    "VideoView_player_AndroidMediaPlayer": "Reproductor: AndroidMediaPlayer",
    "VideoView_player_IjkExoMediaPlayer": "Reproductor: IjkExoMediaPlayer",
    "VideoView_player_IjkMediaPlayer": "Reproductor: IjkMediaPlayer",
    "VideoView_player_none": "Reproductor: Ninguno",
    "VideoView_render_none": "Render: Ninguno",
    "VideoView_render_surface_view": "Render: SurfaceView",
    "VideoView_render_texture_view": "Render: TextureView",
}

def update_strings_es():
    f = APK_RES / "values-es/strings.xml"
    if not f.exists():
        print(f"[ERROR] No existe {f}")
        return
    
    content = f.read_text(encoding="utf-8")
    new_strings = []
    
    for key, value in ES_TRANSLATIONS.items():
        pattern = re.compile(rf'<string name="{re.escape(key)}"')
        if pattern.search(content):
            continue
        safe_value = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        new_strings.append(f'    <string name="{key}">{safe_value}</string>')
    
    if not new_strings:
        print("[OK] values-es/strings.xml ya tenía todas las traducciones")
        return
    
    block = "\n    <!-- ===== Traducciones ES adicionales (Xuper Hydra redesign) ===== -->\n"
    block += "\n".join(new_strings)
    block += "\n"
    
    new_content = content.replace("</resources>", block + "</resources>", 1)
    f.write_text(new_content, encoding="utf-8")
    print(f"[OK] values-es/strings.xml: {len(new_strings)} traducciones nuevas agregadas")

# ============================================================
# Asegurar values-en/ explícito para auto-detección
# ============================================================
def ensure_values_en():
    en_dir = APK_RES / "values-en"
    en_dir.mkdir(exist_ok=True)
    
    src = APK_RES / "values/strings.xml"
    dst = en_dir / "strings.xml"
    
    if not dst.exists():
        dst.write_text(src.read_text(encoding="utf-8"), encoding="utf-8")
        print(f"[OK] values-en/strings.xml creado (copia explícita del inglés)")
    else:
        print(f"[OK] values-en/strings.xml ya existe")

# ============================================================
# MAIN
# ============================================================
if __name__ == "__main__":
    print("=" * 60)
    print("XUPER HYDRA — Rediseño visual iteración 1 (v2)")
    print("=" * 60)
    print("\n[1/4] Actualizando colors.xml...")
    update_colors()
    print("\n[2/4] Migrando BaseAppTheme a tema oscuro...")
    update_base_theme()
    print("\n[3/4] Completando traducciones ES...")
    update_strings_es()
    print("\n[4/4] Asegurando values-en/ explícito...")
    ensure_values_en()
    print("\n" + "=" * 60)
    print("✅ Cambios aplicados. Listo para recompilar con apktool b")
