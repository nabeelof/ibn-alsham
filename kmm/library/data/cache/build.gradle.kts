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
            api(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.collections.immutable)
            api(libs.koin.core)
        }
    }
}

dependencies {
    // AppCompatDelegate backs the per-app locale read in PlatformInfo.
    "androidMainImplementation"("androidx.appcompat:appcompat:1.7.0")
}

android {
    namespace = "com.ibnalsham.app.library.data.cache"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
}
