repositories {
    google {
        content {
            includeGroupByRegex("com\\.android.*")
            includeGroupByRegex("com\\.google.*")
            includeGroupByRegex("androidx.*")
        }
    }
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    // compileOnly, а не implementation: AGP/KGP/Compose уже есть на classpath корневого проекта, дублировать не надо
    compileOnly(libs.gradle.plugin.detekt)
    compileOnly(libs.gradle.plugin.android)
    // DSL-интерфейсы AGP (compileSdk/minSdk/androidResources) лежат в gradle-api, а не в gradle.
    compileOnly(libs.gradle.plugin.android.api)
    compileOnly(libs.gradle.plugin.kotlin)
    compileOnly(libs.gradle.plugin.compose)
}

gradlePlugin {
    plugins {
        register("honest-sign-detekt-analysis") {
            id = name
            implementationClass = "plugin.DetektAnalysisPlugin"
        }
        register("honest-sign-detekt-rule") {
            id = name
            implementationClass = "plugin.DetektRulePlugin"
        }
        register("honest-sign-kmp-library") {
            id = name
            implementationClass = "plugin.SanbaiKmpLibraryPlugin"
        }
        register("honest-sign-kmp-compose") {
            id = name
            implementationClass = "plugin.SanbaiKmpComposePlugin"
        }
        register("honest-sign-kmp-feature") {
            id = name
            implementationClass = "plugin.SanbaiKmpFeaturePlugin"
        }
    }
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}