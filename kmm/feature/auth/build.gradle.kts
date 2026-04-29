plugins { id("convention.kmp-library") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":kmm:data:auth-api"))
            implementation(project(":kmm:library:data:cache"))
            api(project(":kmm:library:presentation:mvi"))
            implementation(project(":kmm:library:data:network-error"))
            api(project(":kmm:library:core:common"))
            api(libs.kotlinx.collections.immutable)
        }
    }
}
