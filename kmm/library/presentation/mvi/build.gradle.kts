plugins { id("convention.kmp-library") }

kotlin {
    sourceSets {
        androidMain.dependencies {
            api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
        }
    }
}
