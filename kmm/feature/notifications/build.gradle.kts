plugins { id("convention.kmp-library") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":kmm:data:notification-api"))
            api(project(":kmm:library:presentation:mvi"))
            implementation(project(":kmm:library:data:network-error"))
            api(project(":kmm:library:core:common"))
            api(project(":kmm:feature:domain"))
            api(project(":kmm:library:data:cache"))
            api(libs.kotlinx.collections.immutable)
        }
    }
}
