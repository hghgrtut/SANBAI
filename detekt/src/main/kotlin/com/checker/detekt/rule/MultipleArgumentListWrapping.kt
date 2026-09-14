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
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.getCallNameExpression

internal class MultipleArgumentListWrapping(config: Config) : Rule(config) {
    override val issue = Issue(javaClass.simpleName, Severity.Style, DESCRIPTION, Debt.FIVE_MINS)

    @Configuration("number of arguments that triggers this inspection")
    private val threshold: Int by config(defaultValue = 2)

    override fun visitCallExpression(expression: KtCallExpression) {
        if (expression.valueArguments.size >= threshold && expression.shouldWrapArguments()) {
            val name = expression.getCallNameExpression()?.getReferencedName() ?: return
            report(
                CorrectableCodeSmell(
                    issue = issue,
                    entity = Entity.from(expression),
                    message = """Call of "$name" has wrong arguments formatting""",
                    autoCorrectEnabled = autoCorrect,
                ),
            )

            withAutoCorrect {
                autoCorrect(expression)
            }
        } else {
            super.visitCallExpression(expression)
        }
    }

    private fun KtCallExpression.shouldWrapArguments(): Boolean {
        if (!valueArguments.all { it.isNamed() }) return false
        val text = valueArguments.fold(valueArgumentList!!.text) { text, param ->
            text.replaceFirst(param.text, "ARG")
        }
        return !text.matches(regex)
    }

    private fun autoCorrect(expression: KtCallExpression) {
        val list = expression.valueArgumentList ?: return
        val ktPsiFactory = KtPsiFactory.contextual(expression)

        val indent = expression.node.lineIndent()
        val replaceText = list.arguments.joinToString(",\n$indent    ", "(\n$indent    ", "\n$indent)") { it.text }
        val code = expression.text.replaceFirst(list.text, replaceText)
        expression.replace(ktPsiFactory.createExpression(code))
    }

    companion object {
        private val regex = """
            \(
            # комментарии
            (?:(?:\s*//[^\n]*)?\n)+
            (
            # аргумент с обязательной запятой
            \s*ARG,
            # комментарии
            (?:(?:\s*//[^\n]*)?\n)+
            )*
            # аргумент с необязательной запятой
            \s*ARG,?
            # комментарии
            (?:(?:\s*//[^\n]*)?\n)+
            # отступ перед закрывающей скобкой
            \s*
            \)
        """
            .trimIndent()
            .toRegex(RegexOption.COMMENTS)

        private const val DESCRIPTION = "Reports wrong arguments formatting"
    }
}
