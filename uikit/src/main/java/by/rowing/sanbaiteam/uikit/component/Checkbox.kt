package by.rowing.sanbaiteam.uikit.component

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.uikit.ComposePreviewWrapper
import by.rowing.sanbaiteam.uikit.R
import by.rowing.sanbaiteam.uikit.theme.ColorPalette
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import com.checker.uikit3.modifier.debounceClickable

@Composable
fun Checkbox(
    modifier: Modifier = Modifier,
    label: String,
    subtitle: String = "",
    checked: CheckedState,
    error: Boolean = false,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(Spacing.SM),
    click: (() -> Unit),
) {
    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(SHAPE)
            .debounceClickable(onClick = click)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
        verticalAlignment = if (subtitle.isBlank()) Alignment.CenterVertically else Alignment.Top
    ) {
        Checkbox(
            modifier = Modifier,
            checked = checked,
            error = error,
            enabled = enabled,
            click = click,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.XS)
        ) {
            Text(
                text = label,
                style = TypographyPalette.Body2Regular,
                color = if (enabled) ColorPalette.Grey20 else ColorPalette.Grey70,
            )

            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = TypographyPalette.Body3Regular,
                    color = if (enabled) ColorPalette.Grey50 else ColorPalette.Grey70
                )
            }
        }
    }
}

@Composable
fun Checkbox(
    modifier: Modifier = Modifier,
    checked: CheckedState,
    error: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    click: (() -> Unit)? = null,
) {
    val colors = CheckboxColors(checked, error, enabled)
    val borderColor by animateColorAsState(colors.border)
    val backgroundColor by animateColorAsState(colors.background)
    val iconTint by animateColorAsState(colors.iconTint)

    Box(
        modifier = modifier
            .size(Spacing.ML)
            .debounceClickable(
                enabled = true,
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = false,
                    radius = RIPPLE_RADIUS
                ),
                onClick = {
                    if (enabled) click?.invoke()
                }
            )

            .fillMaxSize()
            .clip(SHAPE)
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = SHAPE),
        contentAlignment = Alignment.Center
    ) {
        val resId = when (checked) {
            CheckedState.UNCHECKED -> null
            CheckedState.CHECKED -> R.drawable.ic_check_16
            CheckedState.INDETERMINATE -> R.drawable.ic_minus_16
        }

        AnimatedContent(
            modifier = Modifier.size(ICON_SIZE),
            targetState = resId,
            contentAlignment = Alignment.Center,
        ) { id ->
            id?.let {
                Icon(
                    modifier = Modifier.size(ICON_SIZE),
                    painter = painterResource(id),
                    contentDescription = null,
                    tint = iconTint,
                )
            }
        }
    }
}

private val SHAPE = RoundedCornerShape(4.dp)
private val RIPPLE_RADIUS = 19.dp
private val ICON_SIZE = 16.dp

enum class CheckedState {
    CHECKED,
    UNCHECKED,
    INDETERMINATE,
}

private class CheckboxColors(
    val border: Color,
    val background: Color,
    val iconTint: Color,
)

private fun CheckboxColors(
    checked: CheckedState,
    error: Boolean,
    enabled: Boolean,
): CheckboxColors = when {
    error -> CheckboxColors(
        border = ColorPalette.Red50,
        background = ColorPalette.Transparent,
        iconTint = ColorPalette.Transparent,
    )

    !enabled && checked == CheckedState.UNCHECKED -> CheckboxColors(
        border = ColorPalette.Grey80,
        background = ColorPalette.Transparent,
        iconTint = ColorPalette.Transparent,
    )

    !enabled -> CheckboxColors(
        border = ColorPalette.Grey90,
        background = ColorPalette.Grey90,
        iconTint = ColorPalette.Grey60,
    )

    else -> when (checked) {
        CheckedState.UNCHECKED -> CheckboxColors(
            border = ColorPalette.Grey20,
            background = ColorPalette.Transparent,
            iconTint = ColorPalette.Transparent,
        )

        else -> CheckboxColors(
            border = ColorPalette.Grey20,
            background = ColorPalette.Grey20,
            iconTint = ColorPalette.White,
        )
    }
}

@Preview
@Composable
private fun CheckboxPreview() {
    ComposePreviewWrapper {
        var checked by remember { mutableStateOf(CheckedState.UNCHECKED) }
        val context = LocalContext.current

        fun onClick() {
            Toast.makeText(context, checked.toString(), Toast.LENGTH_SHORT).show()
            checked = if (checked == CheckedState.CHECKED) CheckedState.UNCHECKED else CheckedState.CHECKED
        }

        @Composable
        fun Checkbox(
            checked: CheckedState,
            click: () -> Unit,
            modifier: Modifier = Modifier,
            label: String = "Label",
            subtitle: String = "Subtite",
        ) {
            Checkbox(
                modifier = modifier,
                label = label,
                subtitle = subtitle,
                checked = checked,
                error = false,
                enabled = true,
                click = click,
            )
        }

        Column {
            Checkbox(checked, ::onClick, subtitle = "")
            Checkbox(checked, ::onClick)
            Checkbox(checked, ::onClick, modifier = Modifier.fillMaxWidth())
            Checkbox(checked, ::onClick, label = "Label ".repeat(20))
            Row {
                Checkbox(checked, ::onClick)
                Checkbox(checked, ::onClick)
            }
            Row {
                Checkbox(checked, ::onClick, modifier = Modifier.weight(1f))
                Checkbox(checked, ::onClick, modifier = Modifier.weight(1f), subtitle = "Subtitle ".repeat(20))
            }
        }
    }
}
