package plugin

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

/** Ключи из `gradle/libs.versions.toml` → `[versions]`, от которых зависят все KMP-модули. */
internal object SanbaiCatalogKey {
    const val COMPILE_SDK = "compileSdk"
    const val MIN_SDK = "minSdk"
    const val JVM_TARGET = "jvmTarget"
    const val KOTLIN = "kotlin"
}

private const val CATALOG_NAME = "libs"

internal fun Project.sanbaiVersion(key: String): String =
    extensions.getByType<VersionCatalogsExtension>().named(CATALOG_NAME).sanbaiVersion(key)

internal fun Project.sanbaiLibrary(alias: String): Provider<MinimalExternalModuleDependency> =
    extensions.getByType<VersionCatalogsExtension>().named(CATALOG_NAME).sanbaiLibrary(alias)

/** Ищет версию по точному ключу, затем по kebab-case (каталог нормализует алиасы). */
private fun VersionCatalog.sanbaiVersion(key: String): String {
    val candidates = listOf(key, key.kebabCase())
    return candidates.firstNotNullOfOrNull { candidate ->
        findVersion(candidate).orElse(null)?.requiredVersion
    } ?: throw GradleException("libs.versions.toml: нет версии '$key' (пробовали: ${candidates.joinToString()})")
}

/** Ищет библиотеку по точному алиасу, затем по kebab-case. */
private fun VersionCatalog.sanbaiLibrary(alias: String): Provider<MinimalExternalModuleDependency> {
    val candidates = listOf(alias, alias.kebabCase())
    return candidates.firstNotNullOfOrNull { candidate -> findLibrary(candidate).orElse(null) }
        ?: throw GradleException(
            "libs.versions.toml: нет библиотеки '$alias' (пробовали: ${candidates.joinToString()})"
        )
}

private fun String.kebabCase(): String = replace(Regex("([a-z0-9])([A-Z])"), "$1-$2").lowercase()