/*
 * SSL Pinning Bypass para Xuper Hydra (com.xuper.netxxus)
 *
 * Cómo usar:
 *   frida -U -f com.xuper.netxxus -l ssl-bypass.js --no-pause
 *
 * Cobertura:
 *   - OkHttp3 CertificatePinner.check() (la app usa OkHttp3 + Glide integration)
 *   - javax.net.ssl.TrustManager (X509TrustManager)
 *   - javax.net.ssl.SSLContext (SSLContext.init)
 *   - javax.net.ssl.HostnameVerifier
 *   - Conscrypt (Android 8+ default)
 *   - Apache HttpClient (legacy, por si acaso)
 *   - WebView SSL error handler
 */

Java.perform(function () {
    console.log('[*] SSL pinning bypass cargado para Xuper Hydra');

    // ========================================================================
    // 1. OkHttp3 CertificatePinner (el más probable en esta app)
    // ========================================================================
    try {
        var CertificatePinner = Java.use('okhttp3.CertificatePinner');
        CertificatePinner.check.overload('java.lang.String', 'java.util.List').implementation = function (hostname, peerCertificates) {
            console.log('[+] OkHttp3 CertificatePinner.check(' + hostname + ') bypassed');
            return;
        };
        // Variante con Certificate[]
        try {
            CertificatePinner.check.overload('java.lang.String', '[Ljava.security.cert.Certificate;').implementation = function (hostname, peerCertificates) {
                console.log('[+] OkHttp3 CertificatePinner.check(' + hostname + ', [Certs]) bypassed');
                return;
            };
        } catch (e) {}
        // Variante nueva (OkHttp 4.x): check$okhttp
        try {
            CertificatePinner['check$okhttp'].implementation = function (hostname, peerCertificates) {
                console.log('[+] OkHttp3 check$okhttp(' + hostname + ') bypassed');
                return;
            };
        } catch (e) {}
    } catch (e) {
        console.log('[-] OkHttp3 CertificatePinner no encontrado: ' + e);
    }

    // ========================================================================
    // 2. TrustManager (X509TrustManager) — acepta cualquier cert
    // ========================================================================
    try {
        var X509TrustManager = Java.use('javax.net.ssl.X509TrustManager');
        var SSLContext = Java.use('javax.net.ssl.SSLContext');

        // Crear un TrustManager que confía en todo
        var TrustManager = Java.registerClass({
            name: 'com.xuper.bypass.TrustAllManager',
            implements: [X509TrustManager],
            methods: {
                checkClientTrusted: function (chain, authType) {},
                checkServerTrusted: function (chain, authType) {},
                getAcceptedIssuers: function () { return []; }
            }
        });

        // Sobreescribir SSLContext.init para usar nuestro TrustManager
        SSLContext.init.overload(
            '[Ljavax.net.ssl.KeyManager;',
            '[Ljavax.net.ssl.TrustManager;',
            'java.security.SecureRandom'
        ).implementation = function (km, tm, sr) {
            console.log('[+] SSLContext.init() bypassed — usando TrustAllManager');
            this.init(km, [TrustManager.$new()], sr);
        };
    } catch (e) {
        console.log('[-] TrustManager bypass failed: ' + e);
    }

    // ========================================================================
    // 3. HostnameVerifier — aceptar cualquier hostname
    // ========================================================================
    try {
        var HostnameVerifier = Java.use('javax.net.ssl.HostnameVerifier');
        var HV = Java.registerClass({
            name: 'com.xuper.bypass.AllowAllHostnameVerifier',
            implements: [HostnameVerifier],
            methods: {
                verify: function (hostname, session) { return true; }
            }
        });

        var HttpsURLConnection = Java.use('javax.net.ssl.HttpsURLConnection');
        HttpsURLConnection.setDefaultHostnameVerifier.implementation = function (v) {
            console.log('[+] HttpsURLConnection.setDefaultHostnameVerifier bypassed');
            this.setDefaultHostnameVerifier(HV.$new());
        };
        HttpsURLConnection.setSSLSocketFactory.implementation = function (f) {
            console.log('[+] HttpsURLConnection.setSSLSocketFactory bypassed (no-op)');
        };
    } catch (e) {
        console.log('[-] HostnameVerifier bypass failed: ' + e);
    }

    // ========================================================================
    // 4. Conscrypt (Android 8+ usa Conscrypt por defecto)
    // ========================================================================
    try {
        var Conscrypt = Java.use('com.android.org.conscrypt.TrustManagerImpl');
        Conscrypt.checkTrusted.overload(
            'java.util.List',
            'java.lang.String',
            'java.lang.String'
        ).implementation = function (chain, authType, host) {
            console.log('[+] Conscrypt TrustManagerImpl.checkTrusted(' + host + ') bypassed');
            return Java.use('java.util.ArrayList').$new();
        };
    } catch (e) {
        // Conscrypt no siempre está disponible
    }

    // ========================================================================
    // 5. OkHttp3 OkHttpClient.Builder con CertificatePinner por defecto
    //    (por si la app construye el cliente con pinning)
    // ========================================================================
    try {
        var Builder = Java.use('okhttp3.OkHttpClient$Builder');
        Builder.certificatePinner.implementation = function (pinner) {
            console.log('[+] OkHttpClient.Builder.certificatePinner() bypassed');
            return this;
        };
        Builder.sslSocketFactory.overload('javax.net.ssl.SSLSocketFactory', 'javax.net.ssl.X509TrustManager').implementation = function (sf, tm) {
            console.log('[+] OkHttpClient.Builder.sslSocketFactory() bypassed');
            return this;
        };
    } catch (e) {}

    // ========================================================================
    // 6. WebView SSL error handler (algunas apps usan WebView)
    // ========================================================================
    try {
        var WebViewClient = Java.use('android.webkit.WebViewClient');
        WebViewClient.onReceivedSslError.implementation = function (view, handler, error) {
            console.log('[+] WebViewClient.onReceivedSslError bypassed');
            handler.proceed();
        };
    } catch (e) {}

    // ========================================================================
    // 7. Apache HttpClient (legacy, por si la app usa HTTP antiquísimo)
    // ========================================================================
    try {
        var AbstractVerifier = Java.use('org.apache.http.conn.ssl.AbstractVerifier');
        AbstractVerifier.verify.overload('java.lang.String', '[Ljava.lang.String', '[Ljava.lang.String').implementation = function (host, cns, subjectAlts) {
            console.log('[+] Apache AbstractVerifier.verify(' + host + ') bypassed');
        };
    } catch (e) {}

    // ========================================================================
    // 8. Hook al Constructor del app para log de inicialización
    // ========================================================================
    try {
        var AppWrapper = Java.use('com.interactive.brasiliptv.app.AppWrapper');
        AppWrapper.onCreate.implementation = function () {
            console.log('[*] AppWrapper.onCreate() detectado — bypass activo');
            return this.onCreate();
        };
    } catch (e) {}

    console.log('[*] SSL pinning bypass completo. Navega la app normalmente.');
});

// ========================================================================
// 9. Hook nativo (por si la app tiene pinning en C/C++ via libssl)
// ========================================================================
// Solo activar si los hooks Java no son suficientes. Requiere frida-server con
// permisos root. Esto suele ser innecesario para OkHttp3-only.

// try {
//     var SSL_CTX_set_verify = Module.findExportByName('libssl.so', 'SSL_CTX_set_verify');
//     if (SSL_CTX_set_verify) {
//         Interceptor.replace(SSL_CTX_set_verify, new NativeCallback(function (ctx, mode, cb) {
//             // SSL_VERIFY_NONE = 0
//             new NativeFunction(SSL_CTX_set_verify, 'void', ['pointer', 'int', 'pointer'])(ctx, 0, ptr(0));
//         }, 'void', ['pointer', 'int', 'pointer']));
//     }
// } catch (e) {}
