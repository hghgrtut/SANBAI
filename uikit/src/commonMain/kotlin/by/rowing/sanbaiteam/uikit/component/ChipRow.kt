package by.rowing.sanbaiteam.uikit.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.ComposePreviewWrapper
import by.rowing.sanbaiteam.uikit.theme.SANBAITeamTheme
import by.rowing.sanbaiteam.uikit.theme.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Горизонтально прокручиваемый ряд чипов с едиными отступами. */
@AllowDetektPublic
@Composable
fun ChipRow(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = Spacing.M),
    spacing: Dp = Spacing.S,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(state = rememberScrollState())
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Preview
@Composable
private fun ChipRowPreview() {
    ComposePreviewWrapper {
        SANBAITeamTheme {
            ChipRow {
                Chip(
                    text = "Все",
                    selected = true,
                    onClick = {}
                )
                Chip(
                    text = "Одиночка",
                    onClick = {}
                )
                Chip(
                    text = "Двойка",
                    onClick = {}
                )
                Chip(
                    text = "Четвёрка",
                    onClick = {}
                )
                Chip(
                    text = "Восьмёрка",
                    onClick = {}
                )
            }
        }
    }
}