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
    add("compileOnly", libs.gradle.plugin.detekt)
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
    }
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}
