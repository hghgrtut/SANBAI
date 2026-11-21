package plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * KMP-модуль с Compose Multiplatform: [SanbaiKmpLibraryPlugin] + Compose-плагины + базовый набор
 * compose-зависимостей в `commonMain` (runtime, foundation, material3, иконки, ui).
 *
 * Зависимость `compose.components.resources` сюда намеренно не входит: она включает генерацию
 * Res-класса, а пакет по умолчанию строится из `project.group`/`project.name` (в корне есть пробел —
 * «SANBAI Team»), что ломает DEX. Модули с `composeResources` добавляют её сами и задают
 * `compose.resources { packageOfResClass = ... }`.
 *
 * `androidResources` включается базовым плагином.
 */
class SanbaiKmpComposePlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply(SanbaiKmpLibraryPlugin::class.java)
        pluginManager.apply(COMPOSE_PLUGIN_ID)
        pluginManager.apply(COMPOSE_COMPILER_PLUGIN_ID)

        val composeDependencies = extensions.getByType<ComposeExtension>().dependencies

        extensions.getByType<KotlinMultiplatformExtension>()
            .sourceSets
            .getByName(COMMON_MAIN_SOURCE_SET)
            .dependencies {
                implementation(composeDependencies.runtime)
                implementation(composeDependencies.foundation)
                implementation(composeDependencies.material3)
                implementation(composeDependencies.materialIconsExtended)
                implementation(composeDependencies.ui)
            }
    }

    companion object {

        private const val COMPOSE_PLUGIN_ID = "org.jetbrains.compose"
        private const val COMPOSE_COMPILER_PLUGIN_ID = "org.jetbrains.kotlin.plugin.compose"
        private const val COMMON_MAIN_SOURCE_SET = "commonMain"
    }
}