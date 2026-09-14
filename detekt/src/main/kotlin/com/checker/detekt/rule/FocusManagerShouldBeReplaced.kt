package com.checker.detekt.rule

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Use old way to hide keyboard to avoid flickering (см. TMR-28841)
 * На некоторых устройствах скрытие клавиатуры через LocalFocusManager clearFocus
 * приводит к миганию, поэтому рекомендуется способ через InputMethodManager (см. Activity.hideKeyboard())
 */
internal class FocusManagerShouldBeReplaced(config: Config) : Rule(config) {
    override val issue = Issue(javaClass.simpleName, Severity.Defect, DESCRIPTION, Debt.FIVE_MINS)

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        processFunction(function)
    }

    private fun processFunction(function: KtNamedFunction) {
        val annotations = function.annotationEntries
        val isComposable = annotations.any { annotationEntry ->
            annotationEntry.shortName?.asString() == COMPOSABLE_ANNOTATION
        }
        if (!isComposable) return

        if (function.text.contains(POTENTIAL_BUG_TEXT)) {
            val name = function.nameAsSafeName.asString()
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(function),
                    message = "Function \'$name\' uses LocalFocusManager.clearFocus(), " +
                            "potential bug TMR-28841",
                    references = emptyList(),
                ),
            )
        }
    }

    private companion object {
        const val POTENTIAL_BUG_TEXT = "clearFocus("
        const val COMPOSABLE_ANNOTATION = "Composable"
        const val DESCRIPTION = "Use old way to hide keyboard to avoid flickering."
    }
}