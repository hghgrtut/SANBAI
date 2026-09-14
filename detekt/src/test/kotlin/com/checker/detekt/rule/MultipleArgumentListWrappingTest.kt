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
internal class MultipleArgumentListWrappingTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `Если аргумент один, игнорировать`() {
        val findings = evalFindings(
            code = """
            fun foo(bar: Int) = Unit

            fun callExpression() = foo(bar = 0)
            """,
        )
        assertEquals(0, findings.size)
    }

    @Test
    fun `Если не все аргументы именованные, игнорировать`() {
        val findings = evalFindings(
            code = """
            fun foo(bar: Int, baz: Int?) = Unit

            fun callExpression() = foo(0, baz = null)
            """,
        )
        assertEquals(0, findings.size)
    }

    @Test
    fun `Если аргументов несколько и каждый с новой строки, игнорировать`() {
        val findings = evalFindings(
            code = """
            fun foo(bar: Int, baz: Int?) = Unit
            
            fun callExpression() = foo(
                bar = 0,
                baz = null
            )
            """,
        )
        assertEquals(0, findings.size)
    }

    @Test
    fun `Если среди аргументов есть комментарии, аргументы корректно отформатированы, игнорировать`() {
        val findings = evalFindings(
            code = """
            fun foo(bar: Int, baz: Int?) = Unit
            
            fun callExpression() = foo(
                bar = 0, // comment
                baz = null
            )
            """,
        )
        assertEquals(0, findings.size)
    }

    @Test
    fun `Если несколько аргументов в одну строку, кинуть ворнинг`() {
        val findings = evalFindings(
            code = """
            fun foo(bar: Int, baz: Int?) = Unit

            fun callExpression() = foo(bar = 0, baz = null)
            """,
        )
        assertEquals(1, findings.size)
        assertEquals("Call of \"foo\" has wrong arguments formatting", findings.first().message)
    }

    @Test
    fun `Если в вызове конструктора несколько аргументов в одну строку, кинуть ворнинг`() {
        val findings = evalFindings(
            code = """
            data class Foo(val bar: Int, val baz: Int?)

            fun callExpression() = Foo(bar = 0, baz = null)
            """,
        )
        assertEquals(1, findings.size)
        assertEquals("Call of \"Foo\" has wrong arguments formatting", findings.first().message)
    }

    private fun evalFindings(
        code: String,
        active: Boolean = true,
    ): List<Finding> {
        val config = TestConfig(
            Config.ACTIVE_KEY to active.toString(),
            Config.AUTO_CORRECT_KEY to "false",
        )
        return MultipleArgumentListWrapping(config)
            .compileAndLintWithContext(env, code)
    }
}
