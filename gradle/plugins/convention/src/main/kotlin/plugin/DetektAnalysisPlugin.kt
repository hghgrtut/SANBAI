package plugin

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

private const val DETEKT_VERSION = "1.23.8"
private const val CODE_CLIMATE_REPORT_VERSION = "1.0.0"

class DetektAnalysisPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val projectPath = layout.projectDirectory.asFile.absolutePath

        extensions.configure<DetektExtension> {
            toolVersion = DETEKT_VERSION
            autoCorrect = System.getenv("CI").toBoolean().not()
            basePath = projectPath
            config.setFrom(files("$projectPath/config/detekt.yml"))
            baseline = file("$projectPath/config/detekt-baseline.xml")
            parallel = true
            source.setFrom(
                fileTree(projectPath) {
                    include("**/src/*/java/**/*.kt")
                    include("**/src/*/kotlin/**/*.kt")
                    exclude("**/build/**")
                    exclude("**/gradle/**")
                    exclude("**/.gradle/**")
                }
            )
        }

        dependencies {
            add("detektPlugins", project(":detekt"))
            add("detektPlugins", "io.gitlab.arturbosch.detekt:detekt-cli:$DETEKT_VERSION")
            add("detektPlugins", "io.gitlab.arturbosch.detekt:detekt-formatting:$DETEKT_VERSION")
            add("detektPlugins", "io.gitlab.arturbosch.detekt:detekt-rules-libraries:$DETEKT_VERSION")
            add("detektPlugins", "io.github.lexa-diky:detekt-code-climate-report:$CODE_CLIMATE_REPORT_VERSION")
        }

        tasks.withType<Detekt>().configureEach {
            dependsOn(":detekt:assemble")
            reports {
                xml.required.set(false)
                html.required.set(true)
                txt.required.set(false)
                sarif.required.set(false)
                custom {
                    reportId = "code-climate"
                    outputLocation.set(layout.buildDirectory.file("reports/detekt/gitlab.json"))
                }
            }
        }
    }
}