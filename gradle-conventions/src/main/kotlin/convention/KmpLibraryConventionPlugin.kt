package convention

import org.gradle.api.Plugin
import org.gradle.api.Project

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("com.android.library")
            // pluginManager.apply("io.gitlab.arturbosch.detekt")

            extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>("kotlin") {
                androidTarget {
                    compilations.all {
                        kotlinOptions {
                            jvmTarget = "17"
                        }
                    }
                }
                
                listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
                    it.binaries.framework {
                        baseName = project.name.replace("-", "_")
                    }
                }

                sourceSets.getByName("commonMain").dependencies {
                    implementation("io.insert-koin:koin-core:3.5.3")
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
                }
            }

            extensions.configure<com.android.build.gradle.LibraryExtension>("android") {
                compileSdk = 34
                defaultConfig {
                    minSdk = 26
                }
                compileOptions {
                    sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
                    targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
                }
                namespace = "com.ibnalsham.app." + project.path.replace(":", ".").replace("-", "").removePrefix(".")
            }
        }
    }
}
