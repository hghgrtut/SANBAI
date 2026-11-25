import plugin.iosFramework

plugins {
    id("honest-sign-kmp-feature")
}

kotlin {
    android { namespace = "by.rowing.sanbaiteam.shared" }

    iosFramework("Shared")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":uikit"))
            implementation(project(":core:util"))
            implementation(project(":core:annotation"))
            implementation(project(":core:navigation"))
            implementation(project(":core:base"))
            implementation(project(":core:file"))
            implementation(project(":data:local"))
            implementation(project(":data:backup"))
            implementation(project(":feature:calculator"))
            implementation(project(":feature:athlete"))
            implementation(project(":feature:training"))
            implementation(project(":feature:settings"))
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
    }
}

compose.resources {
    packageOfResClass = "by.rowing.sanbaiteam.shared.resources"
    publicResClass = true
}