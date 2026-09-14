package com.checker.detekt.rule

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.CorrectableCodeSmell
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.Configuration
import org.jetbrains.kotlin.lexer.KtTokens.INTERNAL_KEYWORD
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTypeAlias
import org.jetbrains.kotlin.psi.addRemoveModifier.addModifier
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import org.jetbrains.kotlin.psi.psiUtil.isTopLevelKtOrJavaMember

/**
 * Top level declarations should not be public.
 *
 * <noncompliant>
 * class A
 * </noncompliant>
 *
 * <compliant>
 * internal class A
 * </compliant>
 */
internal class TopLevelDeclarationsShouldNotBePublic(config: Config) : Rule(config) {
    override val issue = Issue(javaClass.simpleName, Severity.Style, DESCRIPTION, Debt.FIVE_MINS)

    @Configuration("Ignores declarations in the specified packages.")
    private val ignoredPackages by regexListConfig()

    @Configuration("Ignores declarations with the specified names.")
    private val ignoredNames by regexListConfig()

    @Configuration("Ignores declarations with the specified annotations.")
    private val ignoreAnnotated by regexListConfig()

    override fun visit(root: KtFile) {
        val packageName = root.packageDirective?.packageNameExpression?.text
        val ignoredByExceptionList = packageName != null && ignoredPackages.any { it.matches(packageName) }
        val ignoredByAnnotation = root.isIgnoredByAnnotation()
        if (ignoredByExceptionList || ignoredByAnnotation) return
        super.visit(root)
    }

    override fun visitClass(klass: KtClass) {
        if (klass.isTopLevel()) processDeclaration(klass, "Class")
    }

    override fun visitTypeAlias(typeAlias: KtTypeAlias) {
        if (typeAlias.isTopLevel()) processDeclaration(typeAlias, "TypeAlias")
    }

    override fun visitNamedFunction(function: KtNamedFunction) {
        if (function.isTopLevel) processDeclaration(function, "Top level function")
    }

    override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
        if (declaration.isTopLevelKtOrJavaMember()) processDeclaration(declaration, "Declaration")
    }

    private fun processDeclaration(
        declaration: KtNamedDeclaration,
        prefix: String
    ) {
        if (!declaration.isPublic || declaration.isIgnoredByName() || declaration.isIgnoredByAnnotation()) return

        val name = declaration.nameAsSafeName.asString()
        report(
            CorrectableCodeSmell(
                issue = issue,
                entity = Entity.from(declaration),
                message = """$prefix $name should not be public""",
                autoCorrectEnabled = autoCorrect,
            ),
        )
        withAutoCorrect {
            autoCorrect(declaration)
        }
    }

    private fun autoCorrect(declaration: KtNamedDeclaration) {
        addModifier(declaration, INTERNAL_KEYWORD)
    }

    private fun KtAnnotated.isIgnoredByAnnotation(): Boolean {
        return annotationEntries.any { annotation ->
            val annotationName = annotation.shortName?.asString() ?: return@any false
            ignoreAnnotated.any { it.matches(annotationName) }
        }
    }

    private fun KtNamedDeclaration.isIgnoredByName(): Boolean {
        val declarationName = nameAsSafeName.asString()
        return ignoredNames.any { it.matches(declarationName) }
    }

    private companion object {
        const val DESCRIPTION = "Top level declarations should not be public."
    }
}