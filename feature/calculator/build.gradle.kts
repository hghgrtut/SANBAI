import plugin.iosFramework

plugins {
    id("honest-sign-kmp-feature")
}

kotlin {
    android { namespace = "by.rowing.sanbaiteam.calculator" }

    iosFramework("Calculator")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:util"))
            implementation(project(":core:annotation"))
            implementation(project(":core:navigation"))
            implementation(project(":core:base"))
            implementation(project(":uikit"))
            implementation(compose.components.resources)
        }
    }
}

compose.resources {
    packageOfResClass = "by.rowing.sanbaiteam.calculator.resources"
}