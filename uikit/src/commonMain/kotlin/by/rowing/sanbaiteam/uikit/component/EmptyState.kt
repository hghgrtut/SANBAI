package by.rowing.sanbaiteam.uikit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.ComposePreviewWrapper
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.theme.SANBAITeamTheme
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Единое пустое состояние: иконка, заголовок, пояснение и опциональная кнопка.
 * Вызывающий код задаёт размер контейнера через [modifier] (обычно `fillMaxSize()`).
 */
@AllowDetektPublic
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(Spacing.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(EMPTY_STATE_ICON_SIZE),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = EMPTY_STATE_ICON_ALPHA),
            )
            Spacer(modifier = Modifier.height(Spacing.L))
        }
        Text(
            text = title,
            style = TypographyPalette.H4,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        if (description != null) {
            Spacer(modifier = Modifier.height(Spacing.S))
            Text(
                text = description,
                style = TypographyPalette.Body2Regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(Spacing.XL))
            Button(
                text = actionText,
                debounceClick = onActionClick,
            )
        }
    }
}

private val EMPTY_STATE_ICON_SIZE = 96.dp
private const val EMPTY_STATE_ICON_ALPHA = 0.5f

@Preview
@Composable
private fun EmptyStatePreview() {
    ComposePreviewWrapper {
        SANBAITeamTheme {
            EmptyState(
                title = "Нет тренировок",
                description = "Добавьте первую тренировку",
                actionText = "Добавить тренировку",
                onActionClick = {},
            )
        }
    }
}
