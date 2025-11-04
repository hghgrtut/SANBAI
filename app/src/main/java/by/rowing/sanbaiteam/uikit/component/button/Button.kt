@file:Suppress("UsingMaterialAndMaterial3Libraries", "DEPRECATION")

package by.rowing.sanbaiteam.uikit.component.button

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.uikit.component.Throbber
import by.rowing.sanbaiteam.uikit.theme.ColorPalette
import by.rowing.sanbaiteam.uikit.theme.Radius
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPaletteSp
import com.checker.uikit3.modifier.applyIfEnabled
import com.checker.uikit3.modifier.debounceClickable
import kotlin.collections.first
import kotlin.collections.firstOrNull
import kotlin.collections.maxOf
import kotlin.let
import kotlin.ranges.coerceAtLeast
import kotlin.ranges.coerceIn
import androidx.compose.material3.LocalContentColor as LocalMaterial3ContentColor

/**
 * Компонент Button
 *
 * Макет: [figma](https://www.figma.com/design/J0LnHWw2Q3FMDPeIf4Wk2A/UI-Kit-3-Mobile?node-id=2407-925)
 */
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
        @DrawableRes icon: Int,
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
        fun primary(
            enabledText: Color = ColorPalette.White,
            disabledText: Color = ColorPalette.Grey60,
            enabledBackground: Color = ColorPalette.Grey20,
            disabledBackground: Color = ColorPalette.Grey90,
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

        fun secondary(
            enabledText: Color = ColorPalette.Grey20,
            disabledText: Color = ColorPalette.Grey60,
            enabledBackground: Color = Color.Unspecified,
            disabledBackground: Color = ColorPalette.Grey90,
            enabledBorderColor: Color = enabledText,
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

        fun text(
            enabledText: Color = ColorPalette.Grey20,
            disabledText: Color = ColorPalette.Grey60,
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

        val default = primary(
            enabledText = ColorPalette.White,
            disabledText = ColorPalette.Grey60,
            enabledBackground = ColorPalette.Grey20,
            disabledBackground = ColorPalette.Grey90,
        )
        val inverse = primary(
            enabledText = ColorPalette.Grey20,
            disabledText = ColorPalette.Grey70,
            enabledBackground = ColorPalette.White,
            disabledBackground = ColorPalette.Grey40,
        )

        val accent = primary(
            enabledText = ColorPalette.White,
            disabledText = ColorPalette.Grey60,
            enabledBackground = ColorPalette.Blue50,
            disabledBackground = ColorPalette.Grey90,
        )

        val brand = primary(
            enabledText = ColorPalette.Grey20,
            disabledText = ColorPalette.Grey60,
            enabledBackground = ColorPalette.Yellow50New,
            disabledBackground = ColorPalette.Grey90,
        )

        val attention = primary(
            enabledText = ColorPalette.White,
            disabledText = ColorPalette.Grey60,
            enabledBackground = ColorPalette.Red50,
            disabledBackground = ColorPalette.Grey90,
        )

        val secondaryDefault = secondary(
            enabledText = ColorPalette.Grey20,
            disabledText = ColorPalette.Grey60,
        )
        val secondaryInverse = secondary(
            enabledText = ColorPalette.White,
            disabledText = ColorPalette.Grey40,
            disabledBackground = Color.Unspecified,
            disabledBorderColor = ColorPalette.Grey40,
        )
        val secondaryAccent = secondary(
            enabledText = ColorPalette.Blue60,
            disabledText = ColorPalette.Grey60,
        )
        val secondaryBrand = secondary(
            enabledText = ColorPalette.Yellow50,
            disabledText = ColorPalette.Grey40,
            disabledBackground = Color.Unspecified,
            disabledBorderColor = ColorPalette.Grey40,
            throbberStyle = Throbber.Style.YELLOW,
        )
        val secondaryAttention = secondary(
            enabledText = ColorPalette.Red50,
            disabledText = ColorPalette.Grey60,
        )

        val textDefault = text(
            enabledText = ColorPalette.Grey20,
            disabledText = ColorPalette.Grey60,
        )
        val textInverse = text(
            enabledText = ColorPalette.White,
            disabledText = ColorPalette.Grey40,
        )
        val textAccent = text(
            enabledText = ColorPalette.Blue60,
            disabledText = ColorPalette.Grey60,
        )
        val textBrand = text(
            enabledText = ColorPalette.Yellow50New,
            disabledText = ColorPalette.Grey40,
        )
        val textAttention = text(
            enabledText = ColorPalette.Red50,
            disabledText = ColorPalette.Grey60,
        )
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