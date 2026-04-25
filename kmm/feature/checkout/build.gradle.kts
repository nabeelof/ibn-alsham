plugins { id("convention.kmp-library") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":kmm:library:presentation:mvi"))
            implementation(project(":kmm:library:data:network-error"))
            api(project(":kmm:library:core:common"))
            implementation(project(":kmm:data:order-api"))
            implementation(project(":kmm:library:data:cache"))
            api(project(":kmm:feature:cart"))
            api(project(":kmm:feature:domain"))
            api(libs.kotlinx.collections.immutable)
        }
    }
}
