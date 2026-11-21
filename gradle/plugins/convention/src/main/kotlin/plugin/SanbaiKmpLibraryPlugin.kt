package plugin

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/**
 * Базовая конфигурация KMP-модуля: одинаковые настройки, которые раньше копировались в каждый
 * `build.gradle.kts` модуля.
 *
 * Что делает:
 * - применяет `org.jetbrains.kotlin.multiplatform` + `com.android.kotlin.multiplatform.library`;
 * - объявляет iOS-таргеты `iosArm64` / `iosSimulatorArm64` (framework — через [iosFramework] в модуле);
 * - ставит `compileSdk` / `minSdk` из `gradle/libs.versions.toml` и включает `androidResources`;
 * - ставит `jvmTarget` всем JVM-компиляциям модуля;
 * - добавляет `kotlin-test` в `commonTest` и (если модуль включает host-тесты) в `androidHostTest`.
 *
 * В модуле остаётся только уникальное: `namespace` и зависимости.
 */
class SanbaiKmpLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply(KMP_PLUGIN_ID)
        pluginManager.apply(ANDROID_KMP_LIBRARY_PLUGIN_ID)

        val kotlinExtension = extensions.getByType<KotlinMultiplatformExtension>()
        kotlinExtension.iosArm64()
        kotlinExtension.iosSimulatorArm64()

        configureAndroid(kotlinExtension)
        configureJvmTarget()
        configureTests(kotlinExtension)
    }

    private fun Project.configureAndroid(kotlinExtension: KotlinMultiplatformExtension) {
        val androidTarget =
            (kotlinExtension as ExtensionAware).extensions.getByType<KotlinMultiplatformAndroidLibraryTarget>()

        androidTarget.compileSdk = sanbaiVersion(SanbaiCatalogKey.COMPILE_SDK).toInt()
        androidTarget.minSdk = sanbaiVersion(SanbaiCatalogKey.MIN_SDK).toInt()
        androidTarget.androidResources.enable = true
    }

    private fun Project.configureJvmTarget() {
        val jvmTarget = JvmTarget.fromTarget(sanbaiVersion(SanbaiCatalogKey.JVM_TARGET))
        tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions.jvmTarget.set(jvmTarget)
        }
    }

    private fun Project.configureTests(kotlinExtension: KotlinMultiplatformExtension) {
        val kotlinTest = "org.jetbrains.kotlin:kotlin-test:${sanbaiVersion(SanbaiCatalogKey.KOTLIN)}"

        kotlinExtension.sourceSets.getByName(COMMON_TEST_SOURCE_SET).dependencies {
            implementation(kotlinTest)
        }

        // androidHostTest появляется только после withHostTest {} в модуле, поэтому подписываемся на коллекцию.
        kotlinExtension.sourceSets
            .matching { it.name == ANDROID_HOST_TEST_SOURCE_SET }
            .configureEach {
                dependencies {
                    implementation(kotlinTest)
                }
            }
    }

    companion object {

        private const val KMP_PLUGIN_ID = "org.jetbrains.kotlin.multiplatform"
        private const val ANDROID_KMP_LIBRARY_PLUGIN_ID = "com.android.kotlin.multiplatform.library"
        private const val COMMON_TEST_SOURCE_SET = "commonTest"
        private const val ANDROID_HOST_TEST_SOURCE_SET = "androidHostTest"
    }
}