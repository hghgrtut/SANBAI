plugins {
    id("honest-sign-kmp-compose")
}

kotlin {
    android { namespace = "by.rowing.sanbaiteam.core.base" }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:annotation"))
            implementation(libs.jetbrains.lifecycle.viewmodel)
            implementation(libs.jetbrains.lifecycle.runtime)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}