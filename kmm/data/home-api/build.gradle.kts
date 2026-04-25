plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    
    jvmToolchain(17)

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":kmm:library:data:network"))
            implementation(project(":kmm:library:data:network-error"))
            implementation(project(":kmm:feature:domain"))
            implementation(project(":kmm:library:core:common"))
            implementation(libs.kotlinx.collections.immutable)
            
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            api(libs.koin.core)
        }
    }
}

android {
    namespace = "com.ibnalsham.app.data.home"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
}
