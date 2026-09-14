package com.checker.detekt.rule

import io.gitlab.arturbosch.detekt.api.config
import io.gitlab.arturbosch.detekt.api.simplePatternToRegex

internal fun regexListConfig(vararg defaultValue: String) = config(listOf(*defaultValue)) { list ->
    list.distinct().map { it.simplePatternToRegex() }
}
