plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}
android {
    namespace = "com.ibnalsham.app.ui.design.designSystem"
    compileSdk = 34
    defaultConfig { minSdk = 26 }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    api(project(":kmm:library:core:priceFormatter"))

    // The sign-in gate reads session state and the GatedFeature copy keys.
    api(project(":kmm:library:data:cache"))

    // Product/category cards render domain models directly.
    api(project(":kmm:feature:domain"))

    // Feature modules consume the theme, motion and components from here, so the
    // Compose surface has to stay on their compile classpath.
    api(platform(libs.compose.bom))
    api(libs.compose.ui)
    api(libs.compose.ui.graphics)
    api(libs.compose.material3)
    api(libs.compose.icons.extended)
    api(libs.coil.compose)
    implementation(libs.compose.ui.tooling.preview)
}
