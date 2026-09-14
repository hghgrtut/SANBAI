package com.checker.detekt

import com.checker.detekt.rule.FocusManagerShouldBeReplaced
import com.checker.detekt.rule.MultipleArgumentListWrapping
import com.checker.detekt.rule.MultipleParameterListWrapping
import com.checker.detekt.rule.TopLevelDeclarationsShouldNotBePublic
import com.checker.detekt.rule.ViewModelUseInitBlock
import com.checker.detekt.rule.WrongClassDeclarationsOrder
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

internal class HonestsignDetektRulesProvider : RuleSetProvider {

    override val ruleSetId: String = "honestsign-detekt-rules"

    override fun instance(config: Config) = RuleSet(
        ruleSetId,
        listOf(
            TopLevelDeclarationsShouldNotBePublic(config),
            FocusManagerShouldBeReplaced(config),
            ViewModelUseInitBlock(config),
            WrongClassDeclarationsOrder(config),
            MultipleArgumentListWrapping(config),
            MultipleParameterListWrapping(config),
        ),
    )
}
