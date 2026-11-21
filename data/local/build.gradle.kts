import plugin.iosFramework

plugins {
    id("honest-sign-kmp-library")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android { namespace = "by.rowing.sanbaiteam.data.local" }

    iosFramework("DataLocal")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:annotation"))
            implementation(compose.runtime)
            api(compose.components.resources)
            implementation(libs.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
        androidMain.dependencies { implementation(libs.koin.android) }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

compose.resources {
    packageOfResClass = "by.rowing.sanbaiteam.data.local.resources"
}