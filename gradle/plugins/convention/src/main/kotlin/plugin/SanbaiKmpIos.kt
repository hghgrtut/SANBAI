package plugin

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Объявляет iOS-конфигурацию модуля: static framework с именем [name].
 *
 * iOS-таргеты к этому моменту уже созданы [SanbaiKmpLibraryPlugin], вызов второй раз возвращает те же объекты.
 * Используется в модуле одной строкой:
 *
 * ```kotlin
 * kotlin {
 *     iosFramework("Athlete")
 * }
 * ```
 */
fun KotlinMultiplatformExtension.iosFramework(name: String) {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = name
            isStatic = true
        }
    }
}