plugins {
    id("honest-sign-kmp-library")
}

kotlin {
    android {
        namespace = "by.rowing.sanbaiteam.core.util"
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:annotation"))
            implementation(libs.kotlinx.datetime)
        }
        androidMain.dependencies { implementation(libs.androidx.core.ktx) }
    }
}