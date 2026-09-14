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
internal class WrongClassDeclarationsOrderTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `Если приватные функции следуют за публичными, игнорировать`() {
        val findings = evalFindings(
            code = """
            class Foo {
                fun foo1() { }
                fun foo3() { }
                private fun foo2() { }
                private fun foo4() { }
            }
            """,
        )
        assertEquals(0, findings.size)
    }

    @Test
    fun `Если приватные функции перемешаны с публичными, кинуть ворнинг`() {
        val findings = evalFindings(
            code = """
            class Foo {
                fun foo1() { }
                private fun foo2() { }
                fun foo3() { }
                private fun foo4() { }
            }
            """,
        )
        assertEquals(1, findings.size)
    }

    private fun evalFindings(
        code: String,
        active: Boolean = true,
    ): List<Finding> {
        val config = TestConfig(
            Config.ACTIVE_KEY to active.toString(),
        )
        return WrongClassDeclarationsOrder(config)
            .compileAndLintWithContext(env, code)
    }
}
