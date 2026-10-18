package by.rowing.sanbaiteam.uikit.component

import androidx.compose.runtime.Composable
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Строковый ресурс вместе с аргументами форматирования.
 * Хранится в стейте, резолвится в UI через [resolve].
 */
@AllowDetektPublic
class UiText private constructor(
    private val resource: StringResource,
    private val args: List<Any>,
) {

    @Composable
    fun resolve(): String = stringResource(resource, *args.toTypedArray())

    companion object {

        fun of(resource: StringResource, vararg args: Any): UiText = UiText(resource, args.toList())
    }
}
