# Xuper Hydra — ProGuard rules
# Mantener nombres de clases referenciadas desde layouts XML
-keep class com.xuper.netxxus.** { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Gson
-keepattributes Signature
-keep class com.xuper.netxxus.data.model.** { *; }

# Media3
-dontwarn androidx.media3.**
