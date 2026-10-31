package by.rowing.sanbaiteam.uikit.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.ComposePreviewWrapper
import by.rowing.sanbaiteam.uikit.resources.Res
import by.rowing.sanbaiteam.uikit.resources.ic_close_16
import by.rowing.sanbaiteam.uikit.theme.SANBAITeamTheme
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Компактный чип для фильтров и быстрых действий.
 *
 * @param selected подсвечивает чип как активный.
 * @param onLongClick необязательное действие по долгому нажатию.
 * @param onClearClick необязательное действие сброса; показывает крестик справа.
 * @param clearContentDescription описание крестика для доступности.
 */
@OptIn(ExperimentalFoundationApi::class)
@AllowDetektPublic
@Composable
fun Chip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    onLongClick: (() -> Unit)? = null,
    longClickLabel: String? = null,
    onClearClick: (() -> Unit)? = null,
    clearContentDescription: String? = null,
) {
    val containerColor by animateColorAsState(
        targetValue = chipContainerColor(
            selected = selected,
            enabled = enabled
        )
    )
    val contentColor by animateColorAsState(
        targetValue = chipContentColor(
            selected = selected,
            enabled = enabled
        )
    )
    val borderColor by animateColorAsState(
        targetValue = chipBorderColor(
            selected = selected,
            enabled = enabled
        )
    )

    Row(
        modifier = modifier
            .clip(CHIP_SHAPE)
            .combinedClickable(
                enabled = enabled,
                role = Role.Button,
                onLongClickLabel = longClickLabel,
                onLongClick = onLongClick,
                onClick = onClick,
            )
            .background(color = containerColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = CHIP_SHAPE
            )
            .padding(
                horizontal = Spacing.SM,
                vertical = Spacing.S,
            ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(ICON_SIZE),
                tint = contentColor,
            )
        }
        Text(
            text = text,
            style = TypographyPalette.Body2Medium,
            color = contentColor,
        )
        if (onClearClick != null) {
            Icon(
                painter = painterResource(Res.drawable.ic_close_16),
                contentDescription = clearContentDescription,
                modifier = Modifier
                    .size(ICON_SIZE)
                    .clip(CLEAR_SHAPE)
                    .clickable(
                        enabled = enabled,
                        onClick = onClearClick
                    ),
                tint = contentColor,
            )
        }
    }
}

@Composable
private fun chipContainerColor(
    selected: Boolean,
    enabled: Boolean
): Color = when {
    !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = DISABLED_ALPHA)
    selected -> MaterialTheme.colorScheme.primaryContainer
    else -> MaterialTheme.colorScheme.surface
}

@Composable
private fun chipContentColor(
    selected: Boolean,
    enabled: Boolean
): Color {
    val colorScheme = MaterialTheme.colorScheme
    return when {
        !enabled -> colorScheme.onSurfaceVariant.copy(alpha = DISABLED_ALPHA)
        selected -> colorScheme.onPrimaryContainer
        else -> colorScheme.onSurface
    }
}

@Composable
private fun chipBorderColor(
    selected: Boolean,
    enabled: Boolean
): Color = when {
    !enabled -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = DISABLED_ALPHA)
    selected -> Color.Transparent
    else -> MaterialTheme.colorScheme.outlineVariant
}

private val CHIP_SHAPE = RoundedCornerShape(percent = 50)
private val CLEAR_SHAPE = RoundedCornerShape(Spacing.S)
private val ICON_SIZE = 16.dp
private const val DISABLED_ALPHA = 0.5f

@Preview
@Composable
private fun ChipPreview() {
    ComposePreviewWrapper {
        SANBAITeamTheme {
            Row(
                modifier = Modifier.padding(Spacing.M),
                horizontalArrangement = Arrangement.spacedBy(Spacing.S),
            ) {
                Chip(
                    text = "Спортсмен",
                    onClick = {}
                )
                Chip(
                    text = "Тип: Одиночка",
                    selected = true,
                    onClick = {},
                    onClearClick = {},
                    clearContentDescription = "Сбросить тип",
                )
                Chip(
                    text = "Быстрый фильтр",
                    onClick = {},
                    onLongClick = {},
                    longClickLabel = "Управление фильтром",
                )
            }
        }
    }
}