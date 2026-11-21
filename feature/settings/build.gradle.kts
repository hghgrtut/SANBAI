import plugin.iosFramework

plugins {
    id("honest-sign-kmp-feature")
}

kotlin {
    android {
        namespace = "by.rowing.sanbaiteam.settings"
        withHostTest {}
    }

    iosFramework("Settings")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":data:local"))
            implementation(project(":core:util"))
            implementation(project(":core:annotation"))
            implementation(project(":core:navigation"))
            implementation(project(":core:base"))
            implementation(project(":uikit"))
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

compose.resources {
    packageOfResClass = "by.rowing.sanbaiteam.settings.resources"
    publicResClass = true
}