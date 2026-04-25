plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
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
            implementation(project(":kmm:feature:domain")) // For ProductDomain
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.collections.immutable)
            api(libs.koin.core)
        }
    }
}

android {
    namespace = "com.ibnalsham.app.library.data.cache"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
}
