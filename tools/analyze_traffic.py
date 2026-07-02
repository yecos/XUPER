#!/usr/bin/env python3
"""
Analizador de tráfico capturado por mitmproxy para Xuper Hydra.

Uso:
    python3 tools/analyze_traffic.py <archivo.flow o .har>

Genera:
    - docs/ENDPOINTS-CAPTURADOS.md  ← reporte Markdown con todos los endpoints
    - Imprime resumen en consola

Filtra automáticamente:
    - Servicios de Google (Firebase, Analytics, Crashlytics, etc.)
    - Umeng push
    - Taobao/Aliyun
    - Recursos estáticos (imágenes, fonts, etc.)
"""
import sys
import json
import os
from pathlib import Path
from urllib.parse import urlparse, parse_qs
from collections import defaultdict
from datetime import datetime

# Filtrar estos dominios (no son de Xuper)
EXCLUDED_DOMAINS = {
    'googleapis.com', 'google.com', 'google-analytics.com', 'googletagmanager.com',
    'firebase', 'crashlytics', 'firebaselogging',
    'umeng.com', 'umengcloud.com', 'umeng.co',
    'taobao.com', 'aliyun.com', 'alibaba.com', 'amap.com',
    'bytedance.com', 'byteoversea.com',
    'app-measurement.com', 'gstatic.com', 'googlesyndication.com',
    'w3.org', 'schemas.android.com',
    'baidu.com', 'qq.com',
    'sentry.io', 'bugly.com',
    'akamaihd.net', 'cloudfront.net',  # CDN de imágenes
    'itunes.apple.com', 'play.google.com',
}

# Patrones de URL que sugieren endpoints de API
API_PATTERNS = [
    '/api/', '/v1/', '/v2/', '/v3/', '/portal/', '/epg/', '/vod/',
    '/live/', '/login', '/auth', '/user', '/account', '/token',
    '/upgrade', '/notice', '/check', '/register', '/verify',
    '/dcs', '/dccore', '/diamond', '/bigbee', '/market', '/datacollect',
    '/config', '/settings', '/search', '/category', '/detail',
    '/channel', '/program', '/stream', '/play', '/hls/', '/dash/',
]

# Headers que nos interesan
INTERESTING_HEADERS = [
    'authorization', 'x-auth-token', 'x-token', 'x-api-key',
    'cookie', 'user-agent', 'x-portal-key', 'x-device-id',
    'x-app-version', 'x-platform', 'content-type', 'accept',
]


def is_xuper_endpoint(url):
    """Filtra URLs que NO son de Xuper (Google, Umeng, etc.)"""
    try:
        parsed = urlparse(url)
        host = parsed.netloc.lower()
        for excluded in EXCLUDED_DOMAINS:
            if excluded in host:
                return False
        return True
    except:
        return False


def is_api_endpoint(url):
    """Detecta si la URL parece un endpoint de API (no un asset estático)"""
    try:
        parsed = urlparse(url)
        path = parsed.path.lower()
        # Excluir assets estáticos
        if any(path.endswith(ext) for ext in ['.png', '.jpg', '.jpeg', '.gif', '.webp',
                                                '.css', '.js', '.woff', '.woff2',
                                                '.mp4', '.m3u8', '.ts', '.mpd',
                                                '.ico', '.svg', '.otf', '.ttf']):
            return False
        # Incluir si parece API
        if any(pattern in path for pattern in API_PATTERNS):
            return True
        # Incluir si tiene query params (suele ser API)
        if parsed.query:
            return True
        # Incluir si es POST/PUT/DELETE (no suele ser asset)
        return False
    except:
        return False


def load_flow(path):
    """Carga archivo .flow de mitmproxy"""
    from mitmproxy.io import FlowReader
    flows = []
    with open(path, 'rb') as f:
        reader = FlowReader(f)
        for flow in reader.stream():
            if hasattr(flow, 'request') and flow.request:
                flows.append(flow)
    return flows


def load_har(path):
    """Carga archivo .har (HTTP Archive)"""
    with open(path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    return data.get('log', {}).get('entries', [])


def extract_endpoints_from_flow(flows):
    """Extrae endpoints únicos del .flow"""
    endpoints = defaultdict(list)
    for flow in flows:
        req = flow.request
        url = req.pretty_url
        if not is_xuper_endpoint(url):
            continue
        # Clave: método + host + path (sin query)
        parsed = urlparse(url)
        key = f"{req.method} {parsed.netloc}{parsed.path}"
        # Guardar info del endpoint
        info = {
            'method': req.method,
            'url': url,
            'host': parsed.netloc,
            'path': parsed.path,
            'query': parsed.query,
            'query_params': dict(parse_qs(parsed.query)),
            'request_headers': {k.lower(): v for k, v in req.headers.items()},
            'request_body': req.get_text(strict=False) if req.content else None,
            'status': flow.response.status_code if flow.response else None,
            'response_headers': {k.lower(): v for k, v in flow.response.headers.items()} if flow.response else {},
            'response_body': flow.response.get_text(strict=False) if flow.response and flow.response.content else None,
            'response_content_type': flow.response.headers.get('content-type', '') if flow.response else '',
        }
        endpoints[key].append(info)
    return endpoints


def extract_endpoints_from_har(entries):
    """Extrae endpoints únicos del .har"""
    endpoints = defaultdict(list)
    for entry in entries:
        req = entry.get('request', {})
        url = req.get('url', '')
        if not is_xuper_endpoint(url):
            continue
        method = req.get('method', 'GET')
        parsed = urlparse(url)
        key = f"{method} {parsed.netloc}{parsed.path}"

        resp = entry.get('response', {})
        info = {
            'method': method,
            'url': url,
            'host': parsed.netloc,
            'path': parsed.path,
            'query': parsed.query,
            'query_params': dict(parse_qs(parsed.query)),
            'request_headers': {h['name'].lower(): h['value'] for h in req.get('headers', [])},
            'request_body': req.get('postData', {}).get('text') if req.get('postData') else None,
            'status': resp.get('status'),
            'response_headers': {h['name'].lower(): h['value'] for h in resp.get('headers', [])},
            'response_body': resp.get('content', {}).get('text'),
            'response_content_type': resp.get('content', {}).get('mimeType', ''),
        }
        endpoints[key].append(info)
    return endpoints


def truncate(text, max_len=500):
    if not text:
        return ''
    if len(text) > max_len:
        return text[:max_len] + f'\n... [truncado, {len(text)} bytes totales]'
    return text


def is_api_endpoint_call(info):
    """Determina si este endpoint en particular es de API (no asset)"""
    if info['method'] != 'GET':
        return True
    if is_api_endpoint(info['url']):
        return True
    ct = info.get('response_content_type', '').lower()
    if 'json' in ct or 'xml' in ct:
        return True
    return False


def generate_markdown_report(endpoints, output_path):
    """Genera reporte Markdown"""
    now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

    md = []
    md.append(f"# Endpoints capturados — Xuper Hydra\n")
    md.append(f"_Generado: {now}_\n")
    md.append(f"_Total de endpoints únicos: {len(endpoints)}_\n\n")

    # Resumen por host
    md.append("## 📊 Resumen por host\n\n")
    md.append("| Host | Endpoints únicos | Requests totales |\n")
    md.append("|------|------------------|------------------|\n")
    host_stats = defaultdict(lambda: {'endpoints': set(), 'requests': 0})
    for key, calls in endpoints.items():
        host = calls[0]['host']
        host_stats[host]['endpoints'].add(key)
        host_stats[host]['requests'] += len(calls)
    for host, stats in sorted(host_stats.items(), key=lambda x: -x[1]['requests']):
        md.append(f"| `{host}` | {len(stats['endpoints'])} | {stats['requests']} |\n")
    md.append("\n")

    # Endpoints agrupados por host
    md.append("## 🔌 Endpoints de API (filtrados)\n\n")
    md.append("> Se filtran assets estáticos y servicios de terceros (Google/Umeng/etc.)\n\n")

    by_host = defaultdict(dict)
    for key, calls in endpoints.items():
        host = calls[0]['host']
        by_host[host][key] = calls

    for host, host_endpoints in sorted(by_host.items()):
        md.append(f"### `{host}`\n\n")
        for key, calls in sorted(host_endpoints.items()):
            info = calls[0]
            if not is_api_endpoint_call(info):
                continue
            md.append(f"#### `{info['method']}` `{info['path']}`\n\n")
            md.append(f"- **URL completa**: `{info['url']}`\n")
            md.append(f"- **Requests capturados**: {len(calls)}\n")
            md.append(f"- **Status code**: {info['status']}\n")
            if info['query']:
                md.append(f"- **Query string**: `{info['query']}`\n")
            if info['query_params']:
                md.append(f"- **Query params**:\n")
                for k, v in info['query_params'].items():
                    md.append(f"  - `{k}`: `{v[0] if v else ''}`\n")

            # Headers interesantes
            interesting_req_headers = {k: v for k, v in info['request_headers'].items()
                                       if any(h in k for h in INTERESTING_HEADERS)}
            if interesting_req_headers:
                md.append(f"- **Headers de request (auth/identificación)**:\n")
                for k, v in interesting_req_headers.items():
                    md.append(f"  - `{k}`: `{truncate(v, 200)}`\n")

            # Content-Type del response
            if info['response_content_type']:
                md.append(f"- **Response Content-Type**: `{info['response_content_type']}`\n")

            # Request body
            if info['request_body']:
                md.append(f"- **Request body**:\n```json\n{truncate(info['request_body'], 1000)}\n```\n")

            # Response body (solo si es JSON/XML)
            if info['response_body'] and ('json' in info['response_content_type'].lower()
                                           or 'xml' in info['response_content_type'].lower()
                                           or len(info['response_body']) < 2000):
                body_preview = info['response_body']
                # Intentar formatear JSON
                if 'json' in info['response_content_type'].lower():
                    try:
                        body_preview = json.dumps(json.loads(body_preview), indent=2, ensure_ascii=False)
                    except:
                        pass
                md.append(f"- **Response body**:\n```json\n{truncate(body_preview, 1500)}\n```\n")

            md.append("\n---\n\n")

    # Endpoints que NO son de API (assets, etc.) — solo lista
    md.append("## 📦 Otros requests (assets/no-API)\n\n")
    md.append("<details>\n<summary>Ver lista completa</summary>\n\n")
    for key, calls in sorted(endpoints.items()):
        info = calls[0]
        if is_api_endpoint_call(info):
            continue
        md.append(f"- `{info['method']}` `{info['url']}` ({info['status']})\n")
    md.append("\n</details>\n\n")

    # Sugerencias para Retrofit
    md.append("## 💡 Sugerencias para Retrofit\n\n")
    md.append("Basado en los endpoints de API encontrados, esta sería la interfaz sugerida:\n\n")
    md.append("```kotlin\n")
    md.append("interface XuperApi {\n")
    seen_paths = set()
    for key, calls in sorted(endpoints.items()):
        info = calls[0]
        if not is_api_endpoint_call(info):
            continue
        path = info['path']
        if path in seen_paths:
            continue
        seen_paths.add(path)
        # Convertir path a formato Retrofit
        retrofit_path = path
        if info['query_params']:
            # Si hay query params estables, los dejamos en el path con @Query
            for param_name in info['query_params']:
                retrofit_path = retrofit_path  # dejar limpio, @Query se pone en la firma

        method = info['method'].lower()
        method_anno = {'get': 'GET', 'post': 'POST', 'put': 'PUT',
                       'delete': 'DELETE', 'patch': 'PATCH'}.get(method, 'GET')

        # Generar nombre de función del path
        func_parts = [p for p in path.split('/') if p and not p.startswith('{')]
        func_name = '_'.join(func_parts) if func_parts else 'root'
        func_name = func_name.replace('-', '_')

        md.append(f'    @{method_anno}("{retrofit_path}")\n')
        md.append(f'    suspend fun {func_name}(): ResponseBody  // TODO: tipar response\n\n')
    md.append("}\n")
    md.append("```\n\n")

    Path(output_path).write_text(''.join(md), encoding='utf-8')
    print(f"📄 Reporte Markdown generado: {output_path}")


def main():
    if len(sys.argv) < 2:
        print("Uso: python3 analyze_traffic.py <archivo.flow|archivo.har>")
        print("\nFormatos soportados:")
        print("  .flow  → formato binario de mitmproxy (mitmdump -w)")
        print("  .har   → HTTP Archive (exportable desde mitmweb, Chrome DevTools, etc.)")
        sys.exit(1)

    input_file = sys.argv[1]
    if not os.path.exists(input_file):
        print(f"❌ Archivo no encontrado: {input_file}")
        sys.exit(1)

    ext = Path(input_file).suffix.lower()

    print(f"📂 Cargando {input_file}...")

    if ext == '.flow':
        try:
            flows = load_flow(input_file)
            print(f"  {len(flows)} flows cargados")
            endpoints = extract_endpoints_from_flow(flows)
        except ImportError:
            print("❌ Para procesar .flow necesitas mitmproxy:")
            print("   pip install mitmproxy")
            sys.exit(1)
    elif ext == '.har':
        entries = load_har(input_file)
        print(f"  {len(entries)} entries cargadas")
        endpoints = extract_endpoints_from_har(entries)
    else:
        print(f"❌ Formato no soportado: {ext}")
        print("   Formatos válidos: .flow, .har")
        sys.exit(1)

    print(f"✅ {len(endpoints)} endpoints únicos encontrados (después de filtrar)\n")

    # Top 20 endpoints por número de requests
    print("📊 Top 20 endpoints por número de requests:\n")
    sorted_eps = sorted(endpoints.items(), key=lambda x: -len(x[1]))
    for key, calls in sorted_eps[:20]:
        info = calls[0]
        api_marker = "🔌" if is_api_endpoint_call(info) else "📦"
        print(f"  {api_marker} [{len(calls):3d}x] {info['method']:6s} {info['host']}{info['path']}")
        if info['status']:
            print(f"           status: {info['status']} | content-type: {info['response_content_type'][:60]}")

    # Generar reporte
    output_path = Path(__file__).parent.parent / 'docs' / 'ENDPOINTS-CAPTURADOS.md'
    generate_markdown_report(endpoints, str(output_path))

    print(f"\n✅ Análisis completo. Reporte: {output_path}")
    print(f"   Próximo paso: revisar el reporte y mapear endpoints a `app/src/main/java/com/xuper/netxxus/data/api/XuperApi.kt`")


if __name__ == '__main__':
    main()
