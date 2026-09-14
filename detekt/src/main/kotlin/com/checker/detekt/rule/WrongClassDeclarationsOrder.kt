package com.checker.detekt.rule

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.CorrectableCodeSmell
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.Configuration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassInitializer
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isPrivate

internal class WrongClassDeclarationsOrder(config: Config) : Rule(config) {
    override val issue = Issue(javaClass.simpleName, Severity.Style, DESCRIPTION, Debt.FIVE_MINS)

    @Configuration("Ignores declarations in the specified packages.")
    private val ignoredPackages by regexListConfig()

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)

        val packageName = klass.containingKtFile.packageDirective?.packageNameExpression?.text

        val ignoredByPackage = packageName != null && ignoredPackages.any { it.matches(packageName) }
        if (ignoredByPackage) return

        val declarations = klass.body?.declarations.orEmpty().filterNotNull()
        val sorted = declarations.sortedBy { declaration ->
            when (declaration) {
                is KtClassInitializer -> 1
                is KtProperty -> 1
                is KtEnumEntry -> 1
                is KtConstructor<*> -> 2
                is KtNamedFunction -> if (!declaration.isPrivate()) 3 else 4
                is KtObjectDeclaration if (declaration.isCompanion()) -> 5
                else -> Int.MAX_VALUE
            }
        }
        if (declarations != sorted) {
            report(
                CorrectableCodeSmell(
                    issue = issue,
                    entity = Entity.from(klass),
                    metrics = emptyList(),
                    message = """Wrong declarations order in "${klass.name}".""",
                    references = emptyList(),
                    autoCorrectEnabled = autoCorrect,
                ),
            )

            withAutoCorrect {
                autoCorrect(declarations, sorted)
            }
        }
    }

    private fun autoCorrect(
        original: List<KtDeclaration>,
        sorted: List<KtDeclaration>,
    ) {
        original.forEachIndexed { index, declaration ->
            declaration.replace(sorted[index].originalElement.copy())
        }
    }

    private companion object {
        const val DESCRIPTION = "Reports wrong class function order by access level"
    }
}
