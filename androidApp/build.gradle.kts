plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.ibnalsham.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ibnalsham.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // No release keystore yet — see PROFILE_BUILD notes in the README.
        }

        /*
         * Release-like build for performance testing and internal distribution:
         * R8-optimised and non-debuggable, but signed with the debug key so it
         * installs on any device without the (not yet created) release keystore.
         */
        create("profile") {
            initWith(getByName("release"))
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            versionNameSuffix = "-profile"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Firebase Cloud Messaging for order/promotion push notifications.
    // Configured by androidApp/google-services.json (project ibn-alsham-a0d8e).
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-messaging-ktx")

    implementation(project(":kmm:library:di"))
    implementation(project(":ui:design:designSystem"))
    
    // UI Implementation modules
    implementation(project(":ui:feature:splash-impl"))
    implementation(project(":ui:feature:auth-impl"))
    implementation(project(":ui:feature:home-impl"))
    implementation(project(":ui:feature:menu-impl"))
    implementation(project(":ui:feature:cart-impl"))
    implementation(project(":ui:feature:checkout-impl"))
    implementation(project(":ui:feature:orders-impl"))
    implementation(project(":ui:feature:catalog-impl"))
    implementation(project(":ui:feature:search-impl"))
    implementation(project(":ui:feature:productDetails-impl"))
    implementation(project(":ui:feature:more-impl"))
    implementation(project(":ui:feature:settings-impl"))
    implementation(project(":ui:feature:favorites-impl"))
    implementation(project(":ui:feature:profile-impl"))
    implementation(project(":ui:feature:notifications-impl"))
    implementation(libs.compose.icons.extended)
    implementation(project(":ui:design:designSystem"))

    implementation(project(":kmm:feature:navigation"))
    implementation(project(":kmm:data:notification-api"))
    implementation(project(":kmm:library:data:cache"))
    implementation(project(":kmm:library:core:common"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation("com.google.android.material:material:1.11.0")
}
