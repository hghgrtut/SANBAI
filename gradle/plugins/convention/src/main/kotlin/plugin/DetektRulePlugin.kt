package plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

private const val DETEKT_VERSION = "1.23.8"
private const val KOTLIN_VERSION = "2.4.20"
private const val JUNIT_VERSION = "5.11.4"
private const val JDK_VERSION = 17

class DetektRulePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(JDK_VERSION))
        }

        dependencies {
            add("compileOnly", "io.gitlab.arturbosch.detekt:detekt-api:$DETEKT_VERSION")
            add("testImplementation", "org.junit.jupiter:junit-jupiter-api:$JUNIT_VERSION")
            add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine:$JUNIT_VERSION")
            add("testImplementation", "org.jetbrains.kotlin:kotlin-test:$KOTLIN_VERSION")
            add("testImplementation", "io.gitlab.arturbosch.detekt:detekt-test:$DETEKT_VERSION")
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}