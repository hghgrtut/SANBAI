package com.checker.detekt.rule

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtAnonymousInitializer
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.getSuperNames

internal class ViewModelUseInitBlock(config: Config) : Rule(config) {
    override val issue = Issue(javaClass.simpleName, Severity.Defect, DESCRIPTION, Debt.FIVE_MINS)

    override fun visitClass(klass: KtClass) {
        if (klass.getSuperNames().contains("BaseViewModel")) {
            super.visitClass(klass)
        }
    }

    override fun visitAnonymousInitializer(initializer: KtAnonymousInitializer) {
        super.visitAnonymousInitializer(initializer)
        report(
            CodeSmell(
                issue = issue,
                entity = Entity.from(initializer),
                message = DESCRIPTION,
                references = emptyList(),
            ),
        )
        withAutoCorrect {
            autoCorrect(initializer)
        }
    }

    private fun autoCorrect(initializer: KtAnonymousInitializer) {
        val ktPsiFactory = KtPsiFactory.contextual(initializer)
        val code = initializer.text.replace(
            "init {",
            "override fun onCreated(source: androidx.lifecycle.LifecycleOwner) {"
        )
        initializer.replace(ktPsiFactory.createDeclaration<KtNamedFunction>(code))
    }

    private companion object {
        const val DESCRIPTION =
            "Необходимо заменить использование блока init на onCreated."
    }
}
