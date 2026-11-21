import plugin.iosFramework

plugins {
    id("honest-sign-kmp-feature")
}

kotlin {
    android {
        namespace = "by.rowing.sanbaiteam.training"
        withHostTest {}
    }

    iosFramework("Training")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":data:local"))
            implementation(project(":core:util"))
            implementation(project(":core:annotation"))
            implementation(project(":core:file"))
            implementation(project(":core:navigation"))
            implementation(project(":core:base"))
            implementation(project(":uikit"))
            implementation(project(":feature:athlete"))
            implementation(project(":feature:settings"))
            implementation(compose.components.resources)
            implementation(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.activity.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

compose.resources {
    packageOfResClass = "by.rowing.sanbaiteam.training.resources"
}