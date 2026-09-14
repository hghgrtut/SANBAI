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
internal class ViewModelUseInitBlockTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `Если класс не является ViewModel, игнорировать`() {
        val findings = evalFindings(
            code = """
            class Foo {
                init { }
            }
            """,
        )
        assertEquals(0, findings.size)
    }

    @Test
    fun `Если класс наследуется от BaseViewModel и не содержит блок init, игнорировать`() {
        val findings = evalFindings(
            code = """
            class Foo : BaseViewModel() { }
            """,
        )
        assertEquals(0, findings.size)
    }

    @Test
    fun `Если класс наследуется от BaseViewModel и содержит блок init, кинуть ворнинг`() {
        val findings = evalFindings(
            code = """
            class Foo : BaseViewModel() {
                init { }
            }
            """,
        )
        assertEquals(1, findings.size)
    }

    @Test
    fun `Если класс наследуется от ViewModel и содержит блок init, игнорировать`() {
        val findings = evalFindings(
            code = """
            class Foo : ViewModel() {
                init { }
            }
            """,
        )
        assertEquals(0, findings.size)
    }

    private fun evalFindings(
        code: String,
        active: Boolean = true,
    ): List<Finding> {
        val config = TestConfig(
            Config.ACTIVE_KEY to active.toString(),
        )
        return ViewModelUseInitBlock(config)
            .compileAndLintWithContext(env, code)
    }
}
