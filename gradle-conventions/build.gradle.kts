plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.gradle.plugin.kotlin)
    implementation(libs.gradle.plugin.android)
    
    // Core plugins directly available for convention application
    // implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
    // implementation("com.android.tools.build:gradle:8.4.0")
}

gradlePlugin {
    plugins {
        register("kmp-library") {
            id = "convention.kmp-library"
            implementationClass = "convention.KmpLibraryConventionPlugin"
        }
    }
}

