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
            implementation(project(":kmm:library:data:network"))
            implementation(project(":kmm:data:home-api"))
            implementation(project(":kmm:data:menu-api"))
            implementation(project(":kmm:data:auth-api"))
            implementation(project(":kmm:data:catalog-api"))
            implementation(project(":kmm:data:product-api"))
            implementation(project(":kmm:data:search-api"))
            implementation(project(":kmm:data:cart-api"))
            implementation(project(":kmm:data:payment-api"))
            implementation(project(":kmm:data:address-api"))
            implementation(project(":kmm:data:coupon-api"))
            implementation(project(":kmm:data:settings-api"))
            implementation(project(":kmm:data:wallet-api"))
            implementation(project(":kmm:data:upload-api"))
            implementation(project(":kmm:data:review-api"))
            implementation(project(":kmm:data:favorites-api"))
            implementation(project(":kmm:data:profile-api"))
            implementation(project(":kmm:data:notification-api"))

            implementation(project(":kmm:feature:home"))
            implementation(project(":kmm:feature:menu"))
            implementation(project(":kmm:feature:auth"))
            implementation(project(":kmm:feature:cart"))
            implementation(project(":kmm:feature:checkout"))
            implementation(project(":kmm:feature:orders"))
            implementation(project(":kmm:feature:productDetails"))
            implementation(project(":kmm:feature:more"))
            implementation(project(":kmm:feature:catalog"))
            implementation(project(":kmm:feature:search"))
            implementation(project(":kmm:feature:settings"))
            implementation(project(":kmm:feature:favorites"))
            implementation(project(":kmm:feature:profile"))
            implementation(project(":kmm:feature:notifications"))

            implementation(project(":kmm:library:data:cache"))
            implementation(project(":kmm:data:order-api"))
            
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
            
            api(libs.koin.core)
        }
    }
}

android {
    namespace = "com.ibnalsham.app.library.di"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
}
