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
internal class MultipleParameterListWrappingTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `Если параметр один, игнорировать`() {
        val findings = evalFindings(
            code = """
            fun foo(bar: Int) {
                // ...
            }
            """,
        )
        assertEquals(0, findings.size)
    }

    @Test
    fun `Если параметров несколько и каждый с новой строки, игнорировать`() {
        val findings = evalFindings(
            code = """
            fun foo(
                bar: Int,
                baz: Int?
            ) {
                // ...
            }
            """,
        )
        assertEquals(0, findings.size)
    }

    @Test
    fun `Если среди параметров есть комментарии, параметры корректно отформатированы, игнорировать`() {
        val findings = evalFindings(
            code = """
            fun foo(
                // comment
                bar: Int,

                baz: Int? // comment
            ) {
                // ...
            }
            """,
        )
        assertEquals(0, findings.size)
    }

    @Test
    fun `Если параметров несколько в одну строку, кинуть ворнинг`() {
        val findings = evalFindings(
            code = """
            fun foo(bar: Int, baz: Int?) {
                // ...
            }
            """,
        )
        assertEquals(1, findings.size)
        assertEquals("Function \"foo\" has wrong parameters formatting", findings.first().message)
    }

    private fun evalFindings(
        code: String,
        active: Boolean = true,
    ): List<Finding> {
        val config = TestConfig(
            Config.ACTIVE_KEY to active.toString(),
        )
        return MultipleParameterListWrapping(config)
            .compileAndLintWithContext(env, code)
    }
}
