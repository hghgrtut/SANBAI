package com.checker.detekt.rule

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.CorrectableCodeSmell
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.config
import io.gitlab.arturbosch.detekt.api.internal.Configuration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory

internal class MultipleParameterListWrapping(config: Config) : Rule(config) {
    override val issue = Issue(javaClass.simpleName, Severity.Style, DESCRIPTION, Debt.FIVE_MINS)

    @Configuration("number of parameters that triggers this inspection")
    private val threshold: Int by config(defaultValue = 2)

    override fun visitNamedFunction(function: KtNamedFunction) {
        if (function.valueParameters.size >= threshold && function.shouldWrapParameters()) {
            val name = function.nameAsSafeName.asString()
            report(
                CorrectableCodeSmell(
                    issue = issue,
                    entity = Entity.from(function),
                    message = """Function "$name" has wrong parameters formatting""",
                    autoCorrectEnabled = autoCorrect,
                ),
            )

            withAutoCorrect {
                autoCorrect(function)
            }
        } else {
            super.visitNamedFunction(function)
        }
    }

    private fun KtNamedFunction.shouldWrapParameters(): Boolean {
        val text = valueParameters.fold(valueParameterList!!.text) { text, param ->
            text.replaceFirst(param.text, "PARAM")
        }
        return !text.matches(regex)
    }

    private fun autoCorrect(function: KtNamedFunction) {
        val list = function.valueParameterList ?: return
        val ktPsiFactory = KtPsiFactory.contextual(function)

        val indent = function.node.lineIndent()
        val replaceText = list.parameters.joinToString(",\n$indent    ", "(\n$indent    ", "\n$indent)") { it.text }
        val code = function.text.replaceFirst(list.text, replaceText)
        function.replace(ktPsiFactory.createDeclaration<KtNamedFunction>(code))
    }

    companion object {
        private val regex = """
            \(
            # комментарии
            (?:(?:\s*//[^\n]*)?\n)+
            (
            # аргумент с обязательной запятой
            \s*PARAM,
            # комментарии
            (?:(?:\s*//[^\n]*)?\n)+
            )*
            # аргумент с необязательной запятой
            \s*PARAM,?
            # комментарии
            (?:(?:\s*//[^\n]*)?\n)+
            # отступ перед закрывающей скобкой
            \s*
            \)
        """
            .trimIndent()
            .toRegex(RegexOption.COMMENTS)

        private const val DESCRIPTION = "Reports wrong parameters formatting"
    }
}
