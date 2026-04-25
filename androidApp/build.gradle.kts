plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
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
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    implementation(libs.compose.icons.extended)
    implementation(project(":ui:design:designSystem"))

    implementation(project(":kmm:feature:navigation"))

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
