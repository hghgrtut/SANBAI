package by.rowing.sanbaiteam.uikit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.ComposePreviewWrapper
import by.rowing.sanbaiteam.uikit.theme.SANBAITeamTheme
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Строка «подпись — значение»: используется в карточках деталей и сводках.
 */
@AllowDetektPublic
@Composable
fun MetricRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.M),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(weight = 1f),
            text = label,
            style = TypographyPalette.Body3Regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = TypographyPalette.Body2Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview
@Composable
private fun MetricRowPreview() {
    ComposePreviewWrapper {
        SANBAITeamTheme {
            MetricRow(
                label = "Дистанция",
                value = "2 000 м",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
