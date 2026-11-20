plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    android {
        namespace = "by.rowing.sanbaiteam.core.file"
        compileSdk = 37
        minSdk = 25
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
                }
            }
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:annotation"))
            implementation(compose.runtime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}