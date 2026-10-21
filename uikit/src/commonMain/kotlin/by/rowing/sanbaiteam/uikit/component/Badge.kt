package by.rowing.sanbaiteam.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
 * Компактный бейдж-чип для типа тренировки, пола, возраста и т.п.
 */
@AllowDetektPublic
@Composable
fun Badge(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(color = containerColor)
            .padding(
                horizontal = Spacing.S,
                vertical = Spacing.XS,
            ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(BADGE_ICON_SIZE),
                tint = contentColor,
            )
        }
        Text(
            text = text,
            style = TypographyPalette.Body3Medium,
            color = contentColor,
        )
    }
}

private val BADGE_ICON_SIZE = 14.dp

@Preview
@Composable
private fun BadgePreview() {
    ComposePreviewWrapper {
        SANBAITeamTheme {
            Row(
                modifier = Modifier.padding(Spacing.M),
                horizontalArrangement = Arrangement.spacedBy(Spacing.S),
            ) {
                Badge(text = "Concept2")
                Badge(
                    text = "Одиночка",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
