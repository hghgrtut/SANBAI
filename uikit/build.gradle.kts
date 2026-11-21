import plugin.iosFramework

plugins {
    id("honest-sign-kmp-compose")
}

kotlin {
    android {
        namespace = "by.rowing.sanbaiteam.uikit"
        withHostTest {}
    }

    iosFramework("Uikit")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:annotation"))
            api(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.fragment.ktx)
        }
        iosMain.dependencies { implementation(libs.kotlinx.datetime) }
    }
}

compose.resources {
    packageOfResClass = "by.rowing.sanbaiteam.uikit.resources"
    publicResClass = true
}