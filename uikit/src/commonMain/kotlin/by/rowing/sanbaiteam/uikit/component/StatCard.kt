package by.rowing.sanbaiteam.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.ComposePreviewWrapper
import by.rowing.sanbaiteam.uikit.theme.SANBAITeamTheme
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Компактная карточка со значением и подписью — для дашборда и списков.
 *
 * @param accentColor цвет иконки и её подложки.
 * @param onClick если передан, карточка становится кликабельной.
 */
@AllowDetektPublic
@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
) {
    val shape = MaterialTheme.shapes.extraLarge
    val clickableModifier = if (onClick != null) {
        Modifier
            .clip(shape)
            .clickable(onClick = onClick)
    } else {
        Modifier
    }

    Card(
        modifier = modifier.then(clickableModifier),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing._2XS),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.M),
            verticalArrangement = Arrangement.spacedBy(Spacing.XS),
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(ICON_CONTAINER_SIZE)
                        .background(
                            color = accentColor.copy(alpha = ICON_BACKGROUND_ALPHA),
                            shape = MaterialTheme.shapes.small,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(ICON_SIZE),
                        tint = accentColor,
                    )
                }
            }
            Text(
                text = value,
                style = TypographyPalette.H1,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = TypographyPalette.Body3Regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val ICON_CONTAINER_SIZE = 32.dp
private val ICON_SIZE = 20.dp
private const val ICON_BACKGROUND_ALPHA = 0.12f

@Preview
@Composable
private fun StatCardPreview() {
    ComposePreviewWrapper {
        SANBAITeamTheme {
            StatCard(
                value = "12",
                label = "Тренировок",
                modifier = Modifier.padding(Spacing.M),
            )
        }
    }
}
