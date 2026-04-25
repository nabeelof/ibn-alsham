plugins { id("convention.kmp-library") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":kmm:data:settings-api"))
            api(project(":kmm:feature:domain"))
            api(project(":kmm:library:presentation:mvi"))
            implementation(project(":kmm:library:data:network-error"))
            api(project(":kmm:library:core:common"))
            api(libs.kotlinx.collections.immutable)
        }
    }
}
