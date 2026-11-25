import plugin.iosFramework

plugins {
    id("honest-sign-kmp-library")
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
}

kotlin {
    android {
        namespace = "by.rowing.sanbaiteam.data.backup"
        withHostTest {}
    }

    iosFramework("DataBackup")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:annotation"))
            implementation(project(":core:util"))
            implementation(project(":data:local"))
            implementation(project(":feature:training"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
        commonTest.dependencies { implementation(libs.kotlinx.coroutines.test) }
    }
}