package com.checker.detekt.rule

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Finding
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Suppress("NonAsciiCharacters")
@KotlinCoreEnvironmentTest
internal class TopLevelDeclarationsShouldNotBePublicTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `Если package подходит под исключение, игнорировать`() {
        val findings = evalFindings(
            code = """
            package feature.foo
            class Foo
            """,
            ignoredPackages = listOf("*.foo"),
        )
        assertEquals(0, findings.size)
    }

    @Test
    fun `Если package не подходит под исключение, кинуть ворнинг`() {
        val findings = evalFindings(
            code = """
            package feature.bar
            class Foo
            """,
            ignoredPackages = listOf("*.foo"),
        )
        assertEquals(1, findings.size)
    }

    @Test
    fun `Если имя подходит под исключение, игнорировать`() {
        val findings = evalFindings(
            code = """
            package feature.foo
            import org.koin.dsl.module
            val FooModule = module { }
            """,
            ignoredNames = listOf("*Module"),
        )
        assertEquals(0, findings.size)
    }

    @Test
    fun `Если имя не подходит под исключение, кинуть ворнинг`() {
        val findings = evalFindings(
            code = """
            package feature.foo
            class Foo
            """,
            ignoredNames = listOf("*Module"),
        )
        assertEquals(1, findings.size)
    }

    private fun evalFindings(
        code: String,
        ignoredPackages: List<String> = emptyList(),
        ignoredNames: List<String> = emptyList(),
        active: Boolean = true,
    ): List<Finding> {
        val config = TestConfig(
            Config.ACTIVE_KEY to active.toString(),
            "ignoredPackages" to ignoredPackages,
            "ignoredNames" to ignoredNames,
        )
        return TopLevelDeclarationsShouldNotBePublic(config)
            .compileAndLintWithContext(env, code)
    }
}
