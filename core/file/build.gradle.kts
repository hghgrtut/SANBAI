plugins {
    id("honest-sign-kmp-compose")
}

kotlin {
    android { namespace = "by.rowing.sanbaiteam.core.file" }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:annotation"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }
    }
}