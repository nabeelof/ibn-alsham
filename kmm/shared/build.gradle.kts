import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    
    jvmToolchain(17)

    val xcf = XCFramework("SharedFramework")
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "SharedFramework"
            xcf.add(this)
            
            // Export dependencies to be visible from Swift
            export(project(":kmm:library:di"))
            export(project(":kmm:library:presentation:mvi"))
            export(project(":kmm:feature:navigation"))
            
            // Feature APIs
            export(project(":kmm:data:home-api"))
            export(project(":kmm:data:menu-api"))
            export(project(":kmm:data:auth-api"))
            export(project(":kmm:data:order-api"))
            export(project(":kmm:data:catalog-api"))
            export(project(":kmm:data:product-api"))
            export(project(":kmm:data:search-api"))
            export(project(":kmm:data:cart-api"))
            export(project(":kmm:data:payment-api"))
            export(project(":kmm:data:address-api"))
            export(project(":kmm:data:coupon-api"))
            export(project(":kmm:data:settings-api"))
            export(project(":kmm:data:wallet-api"))
            export(project(":kmm:data:upload-api"))
            export(project(":kmm:data:review-api"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":kmm:library:di"))
            api(project(":kmm:library:presentation:mvi"))
            api(project(":kmm:feature:navigation"))
            
            api(project(":kmm:data:home-api"))
            api(project(":kmm:data:menu-api"))
            api(project(":kmm:data:auth-api"))
            api(project(":kmm:data:order-api"))
            api(project(":kmm:data:catalog-api"))
            api(project(":kmm:data:product-api"))
            api(project(":kmm:data:search-api"))
            api(project(":kmm:data:cart-api"))
            api(project(":kmm:data:payment-api"))
            api(project(":kmm:data:address-api"))
            api(project(":kmm:data:coupon-api"))
            api(project(":kmm:data:settings-api"))
            api(project(":kmm:data:wallet-api"))
            api(project(":kmm:data:upload-api"))
            api(project(":kmm:data:review-api"))
        }
    }
}

android {
    namespace = "com.ibnalsham.app.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
}
