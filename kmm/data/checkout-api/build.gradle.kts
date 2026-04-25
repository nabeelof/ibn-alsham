plugins {
    id("convention.kmp-library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.ibnalsham.app.data.checkoutapi"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":kmm:library:core:common"))
            implementation(project(":kmm:library:data:network"))
            implementation(project(":kmm:library:data:cache"))
            implementation(project(":kmm:library:data:network-error"))
            implementation(project(":kmm:feature:domain"))
            
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
        }
    }
}
