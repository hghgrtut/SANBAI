@file:Suppress("UsingMaterialAndMaterial3Libraries", "DEPRECATION")

package by.rowing.sanbaiteam.uikit.component.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.component.Throbber
import by.rowing.sanbaiteam.uikit.modifier.applyIfEnabled
import by.rowing.sanbaiteam.uikit.modifier.debounceClickable
import by.rowing.sanbaiteam.uikit.theme.ColorPalette
import by.rowing.sanbaiteam.uikit.theme.Radius
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPaletteSp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.material3.LocalContentColor as LocalMaterial3ContentColor

/**
 * Компонент Button
 *
 * Макет: [figma](https://www.figma.com/design/J0LnHWw2Q3FMDPeIf4Wk2A/UI-Kit-3-Mobile?node-id=2407-925)
 */
@AllowDetektPublic
@Composable
fun Button(
    modifier: Modifier = Modifier,
    text: String,
    debounceClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: @Composable (Button.() -> Unit)? = null,
    trailingIcon: @Composable (Button.() -> Unit)? = null,
    size: ButtonSize = ButtonSize.LARGE,
    buttonColors: ButtonColors = ButtonColors.default,
) {
    val backgroundColor by animateColorAsState(buttonColors.background(enabled))
    val borderColor by animateColorAsState(buttonColors.border(enabled))
    val textColor by animateColorAsState(buttonColors.text(enabled))

    ButtonLayout(
        modifier = modifier
            .semantics { role = Role.Button }
            .clip(size.shape)
            .debounceClickable(
                enabled = enabled,
                onClick = debounceClick
            )
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = size.shape
            )
            .padding(size.paddingValues)
            .heightIn(min = ICON_SIZE),
        spacing = Spacing.SM,
    ) {
        leadingIcon?.let {
            IconSlot(
                modifier = Modifier.layoutId(LayoutId.LEADING_ICON),
                textColor = textColor,
                loading = loading,
                icon = leadingIcon,
            )
        }

        trailingIcon?.let {
            IconSlot(
                modifier = Modifier.layoutId(LayoutId.TRAILING_ICON),
                textColor = textColor,
                loading = loading,
                icon = trailingIcon,
            )
        }

        Text(
            modifier = Modifier
                .layoutId(LayoutId.TEXT)
                .applyIfEnabled(loading) {
                    graphicsLayer { alpha = 0f }
                },
            text = text,
            style = TypographyPaletteSp.Body1Medium,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (loading) {
            Throbber(
                modifier = Modifier.layoutId(LayoutId.THROBBER),
                style = buttonColors.throbberStyle,
                color = textColor,
                trackColor = textColor.copy(alpha = THROBBER_TRACK_ALPHA),
            )
        }
    }
}

@Composable
private fun ButtonLayout(
    modifier: Modifier = Modifier,
    spacing: Dp,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        val spacing = spacing.roundToPx()

        val looseConstraints = constraints.copy(
            minWidth = 0,
            minHeight = 0,
        )

        val leadingIconMeasurable = measurables.firstOrNull { it.layoutId == LayoutId.LEADING_ICON }
        val trailingIconMeasurable = measurables.firstOrNull { it.layoutId == LayoutId.TRAILING_ICON }
        val throbberMeasurable = measurables.firstOrNull { it.layoutId == LayoutId.THROBBER }
        val textMeasurable = measurables.first { it.layoutId == LayoutId.TEXT }

        val leadingIconPlaceable = leadingIconMeasurable?.measure(looseConstraints)
        val leadingIconSpace = leadingIconPlaceable?.let { it.width + spacing } ?: 0

        val trailingIconPlaceable = trailingIconMeasurable?.measure(looseConstraints)
        val trailingIconSpace = trailingIconPlaceable?.let { it.width + spacing } ?: 0

        val throbberPlaceable = throbberMeasurable?.measure(looseConstraints)

        val maxTextWidth = constraints.maxWidth - leadingIconSpace - trailingIconSpace
        val textConstraints = looseConstraints.copy(maxWidth = maxTextWidth)
        val textPlaceable = textMeasurable.measure(textConstraints)

        val placeables = listOfNotNull(
            textPlaceable,
            leadingIconPlaceable,
            trailingIconPlaceable,
        )

        val layoutWidth = (textPlaceable.width + leadingIconSpace + trailingIconSpace)
            .coerceAtLeast(constraints.minWidth)
        val layoutHeight = placeables.maxOf { it.height }
            .coerceAtLeast(constraints.minHeight)

        val centeredTextOffset = (layoutWidth + leadingIconSpace - textPlaceable.width) / 2
        val textOffset = centeredTextOffset.coerceIn(
            minimumValue = leadingIconSpace,
            maximumValue = layoutWidth - trailingIconSpace - textPlaceable.width,
        )
        layout(layoutWidth, layoutHeight) {
            leadingIconPlaceable?.place(
                x = textOffset - leadingIconSpace,
                y = (layoutHeight - leadingIconPlaceable.height) / 2,
            )
            textPlaceable.place(
                x = textOffset,
                y = (layoutHeight - textPlaceable.height) / 2,
            )
            throbberPlaceable?.place(
                x = (layoutWidth - throbberPlaceable.height) / 2,
                y = (layoutHeight - throbberPlaceable.height) / 2,
            )
            trailingIconPlaceable?.place(
                x = layoutWidth - trailingIconPlaceable.width,
                y = (layoutHeight - trailingIconPlaceable.height) / 2,
            )
        }
    }
}

private enum class LayoutId {
    LEADING_ICON,
    TEXT,
    THROBBER,
    TRAILING_ICON
}

@Composable
private fun IconSlot(
    modifier: Modifier = Modifier,
    textColor: Color,
    loading: Boolean,
    icon: @Composable Button.() -> Unit,
) {
    ProvideContentColor(textColor) {
        Box(
            modifier = modifier
                .size(ICON_SIZE)
                .applyIfEnabled(loading) {
                    graphicsLayer { alpha = 0f }
                },
            contentAlignment = Alignment.Center
        ) {
            Button.icon()
        }
    }
}

@AllowDetektPublic
object Button {
    /**
     * Иконка для кнопки.
     *
     * @param icon Идентификатор ресурса
     * @param tint Тон иконки, по умолчанию использует цвет текста кнопки.
     * Тонировка не применяется если равен [Color.Unspecified].
     */
    @Composable
    fun Icon(
        icon: DrawableResource,
        tint: Color = LocalContentColor.current,
    ) {
        Icon(
            modifier = Modifier.size(ICON_SIZE),
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
        )
    }
}

@Composable
private fun ProvideContentColor(
    contentColor: Color,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalMaterial3ContentColor provides contentColor,
        LocalContentColor provides contentColor,
        content = content
    )
}

private val ICON_SIZE = 24.dp
private const val DISABLED_TEXT_ALPHA = 0.38f
private const val DISABLED_BACKGROUND_ALPHA = 0.12f
private const val DISABLED_BORDER_ALPHA = 0.24f
private const val THROBBER_TRACK_ALPHA = 0.24f

@AllowDetektPublic
enum class ButtonSize(
    internal val shape: Shape,
    internal val paddingValues: PaddingValues,
) {
    LARGE(
        shape = RoundedCornerShape(Radius._2XL),
        paddingValues = PaddingValues(
            horizontal = Spacing.XL,
            vertical = Spacing.ML,
        ),
    ),
    MEDIUM(
        shape = RoundedCornerShape(20.dp),
        paddingValues = PaddingValues(
            horizontal = Spacing.L,
            vertical = Spacing.M,
        ),
    ),
    SMALL(
        shape = RoundedCornerShape(20.dp),
        paddingValues = PaddingValues(
            horizontal = Spacing.ML,
            vertical = Spacing.SM,
        ),
    ),
}

@AllowDetektPublic
class ButtonColors
@Deprecated("Следует использовать предоставленные в ButtonColors.Companion значения или методы")
constructor(
    val enabledText: Color = ColorPalette.White,
    val disabledText: Color = ColorPalette.Grey60,
    val enabledBackground: Color = ColorPalette.Grey20,
    val disabledBackground: Color = ColorPalette.Grey90,
    val enabledBorderColor: Color = enabledBackground,
    val disabledBorderColor: Color = disabledBackground,
    val throbberStyle: Throbber.Style = Throbber.Style.GREY,
) {
    internal fun background(enabled: Boolean) = if (enabled) enabledBackground else disabledBackground
    internal fun border(enabled: Boolean) = if (enabled) enabledBorderColor else disabledBorderColor
    internal fun text(enabled: Boolean) = if (enabled) enabledText else disabledText

    companion object {
        @Composable
        fun primary(
            enabledText: Color = MaterialTheme.colorScheme.inverseOnSurface,
            disabledText: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_TEXT_ALPHA),
            enabledBackground: Color = MaterialTheme.colorScheme.inverseSurface,
            disabledBackground: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_BACKGROUND_ALPHA),
            enabledBorderColor: Color = enabledBackground,
            disabledBorderColor: Color = disabledBackground,
            throbberStyle: Throbber.Style = Throbber.Style.GREY,
        ) = ButtonColors(
            enabledText = enabledText,
            disabledText = disabledText,
            enabledBackground = enabledBackground,
            disabledBackground = disabledBackground,
            enabledBorderColor = enabledBorderColor,
            disabledBorderColor = disabledBorderColor,
            throbberStyle = throbberStyle,
        )

        @Composable
        fun secondary(
            enabledText: Color = MaterialTheme.colorScheme.onSurface,
            disabledText: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_TEXT_ALPHA),
            enabledBackground: Color = Color.Unspecified,
            disabledBackground: Color = Color.Unspecified,
            enabledBorderColor: Color = enabledText,
            disabledBorderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_BORDER_ALPHA),
            throbberStyle: Throbber.Style = Throbber.Style.GREY,
        ) = ButtonColors(
            enabledText = enabledText,
            disabledText = disabledText,
            enabledBackground = enabledBackground,
            disabledBackground = disabledBackground,
            enabledBorderColor = enabledBorderColor,
            disabledBorderColor = disabledBorderColor,
            throbberStyle = throbberStyle,
        )

        @Composable
        fun text(
            enabledText: Color = MaterialTheme.colorScheme.onSurface,
            disabledText: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_TEXT_ALPHA),
            throbberStyle: Throbber.Style = Throbber.Style.GREY,
        ) = ButtonColors(
            enabledText = enabledText,
            disabledText = disabledText,
            enabledBackground = Color.Unspecified,
            disabledBackground = Color.Unspecified,
            enabledBorderColor = Color.Unspecified,
            disabledBorderColor = Color.Unspecified,
            throbberStyle = throbberStyle,
        )

        val default: ButtonColors
            @Composable get() = primary()

        val inverse: ButtonColors
            @Composable get() = primary(
                enabledText = MaterialTheme.colorScheme.onSurface,
                disabledText = MaterialTheme.colorScheme.onSurfaceVariant,
                enabledBackground = MaterialTheme.colorScheme.surface,
                disabledBackground = MaterialTheme.colorScheme.surfaceVariant,
                enabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
            )

        val accent: ButtonColors
            @Composable get() = primary(
                enabledText = MaterialTheme.colorScheme.onPrimary,
                enabledBackground = MaterialTheme.colorScheme.primary,
            )

        val brand: ButtonColors
            @Composable get() = primary(
                enabledText = MaterialTheme.colorScheme.onTertiaryContainer,
                enabledBackground = MaterialTheme.colorScheme.tertiaryContainer,
            )

        val attention: ButtonColors
            @Composable get() = primary(
                enabledText = MaterialTheme.colorScheme.onError,
                enabledBackground = MaterialTheme.colorScheme.error,
            )

        val secondaryDefault: ButtonColors
            @Composable get() = secondary()

        val secondaryInverse: ButtonColors
            @Composable get() = secondary(
                enabledText = MaterialTheme.colorScheme.inverseOnSurface,
                disabledText = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = DISABLED_TEXT_ALPHA),
                disabledBorderColor = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = DISABLED_BORDER_ALPHA),
            )

        val secondaryAccent: ButtonColors
            @Composable get() = secondary(
                enabledText = MaterialTheme.colorScheme.primary,
                enabledBorderColor = MaterialTheme.colorScheme.primary,
            )

        val secondaryBrand: ButtonColors
            @Composable get() = secondary(
                enabledText = MaterialTheme.colorScheme.tertiary,
                enabledBorderColor = MaterialTheme.colorScheme.tertiary,
                throbberStyle = Throbber.Style.YELLOW,
            )

        val secondaryAttention: ButtonColors
            @Composable get() = secondary(
                enabledText = MaterialTheme.colorScheme.error,
                enabledBorderColor = MaterialTheme.colorScheme.error,
            )

        val textDefault: ButtonColors
            @Composable get() = text()

        val textInverse: ButtonColors
            @Composable get() = text(enabledText = MaterialTheme.colorScheme.inverseOnSurface)

        val textAccent: ButtonColors
            @Composable get() = text(enabledText = MaterialTheme.colorScheme.primary)

        val textBrand: ButtonColors
            @Composable get() = text(enabledText = MaterialTheme.colorScheme.tertiary)

        val textAttention: ButtonColors
            @Composable get() = text(enabledText = MaterialTheme.colorScheme.error)
    }
}

@Preview
@Composable
private fun PrimaryButtonPreview(
    @PreviewParameter(PrimaryButtonPreviewProvider::class)
    state: ButtonPreviewState
) {
    ButtonPreviewContent(state)
}

@Preview
@Composable
private fun SecondaryButtonPreview(
    @PreviewParameter(SecondaryButtonPreviewProvider::class)
    state: ButtonPreviewState
) {
    ButtonPreviewContent(state)
}

@Preview
@Composable
private fun TextButtonPreview(
    @PreviewParameter(TextButtonPreviewProvider::class)
    state: ButtonPreviewState
) {
    ButtonPreviewContent(state)
}