# ── kotlinx.serialization ────────────────────────────────────────────────
# R8 strips the generated serializers, which breaks every API response.
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisible*Annotations, AnnotationDefault
-dontnote kotlinx.serialization.**

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Every @Serializable DTO in this app plus its generated $$serializer.
-keep,includedescriptorclasses class com.ibnalsham.app.**$$serializer { *; }
-keepclassmembers class com.ibnalsham.app.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class com.ibnalsham.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Ktor / OkHttp ────────────────────────────────────────────────────────
-dontwarn org.slf4j.**
-dontwarn kotlinx.coroutines.**
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ── Koin ─────────────────────────────────────────────────────────────────
# Modules resolve by reflection on constructor signatures.
-keep class org.koin.** { *; }
-keepclassmembers class * {
    public <init>(...);
}

# ── Domain & ViewModels ──────────────────────────────────────────────────
# Kept so Koin can construct them and Compose can read their state.
-keep class com.ibnalsham.app.feature.domain.** { *; }
-keep class com.ibnalsham.app.**.presentation.viewModel.** { *; }

# ── Firebase Cloud Messaging ─────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.ibnalsham.app.push.** { *; }
-dontwarn com.google.firebase.**

# ── Kotlin metadata ──────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Metadata { public <methods>; }

# Keep the enum machinery kotlinx.serialization relies on.
-keepclassmembers enum * { *; }
