package plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * KMP-модуль фичи: [SanbaiKmpComposePlugin] + kotlinx-serialization + типовой набор
 * зависимостей экрана (navigation-compose, lifecycle, Koin, coroutines, serialization-json).
 *
 * Модулю остаётся объявить только `namespace`, `iosFramework(...)` (если нужен) и свои project-зависимости.
 */
class SanbaiKmpFeaturePlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply(SanbaiKmpComposePlugin::class.java)
        pluginManager.apply(SERIALIZATION_PLUGIN_ID)

        val featureDependencies = FEATURE_LIBRARIES.map { alias -> sanbaiLibrary(alias) }
        val commonMain = extensions.getByType<KotlinMultiplatformExtension>()
            .sourceSets
            .getByName(COMMON_MAIN_SOURCE_SET)

        commonMain.dependencies {
            featureDependencies.forEach { implementation(it) }
        }
    }

    companion object {

        private const val SERIALIZATION_PLUGIN_ID = "org.jetbrains.kotlin.plugin.serialization"
        private const val COMMON_MAIN_SOURCE_SET = "commonMain"

        private val FEATURE_LIBRARIES = listOf(
            "jetbrains-lifecycle-viewmodel",
            "jetbrains-lifecycle-runtime",
            "jetbrains-lifecycle-viewmodel-compose",
            "jetbrains-lifecycle-runtime-compose",
            "jetbrains-navigation-compose",
            "koin-core",
            "koin-compose",
            "koin-compose-viewmodel",
            "kotlinx-coroutines-core",
            "kotlinx-serialization-json",
        )
    }
}