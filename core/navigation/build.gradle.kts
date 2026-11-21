plugins {
    id("honest-sign-kmp-compose")
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
}

kotlin {
    android { namespace = "by.rowing.sanbaiteam.core.navigation" }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:annotation"))
            api(project(":uikit"))
            implementation(libs.jetbrains.navigation.compose)
            implementation(libs.jetbrains.lifecycle.runtime.compose)
            implementation(libs.jetbrains.lifecycle.viewmodel.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}